package org.jboss.set.mjolnir.server.bean;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.jboss.set.mjolnir.shared.domain.RegisteredUser;
import org.jboss.set.mjolnir.client.exception.ApplicationException;
import org.jboss.set.mjolnir.server.entities.UserEntity;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * {@inheritDoc}
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
@Stateless
public class UserRepositoryBean implements UserRepository {

    private final Logger logger = Logger.getLogger(getClass().getName());

    @Inject
    private EntityManagerFactory entityManagerFactory;

    @Inject
    private LdapRepository ldapRepository;

    @Override
    public RegisteredUser getUser(Long id) {
        EntityManager em = entityManagerFactory.createEntityManager();
        return convertUserEntity(em.find(UserEntity.class, id));
    }

    @Override
    public RegisteredUser getUser(String kerberosName) {
        return getUser(UserName.KERBEROS, kerberosName);
    }

    /**
     * Returns users whose kerberos names are in the given list.
     *
     * @param krbNames list of krb names to search for
     * @return list of users
     */
    List<UserEntity> getUsersByKrbNames(List<String> krbNames) {
        EntityManager em = entityManagerFactory.createEntityManager();

        try {
            TypedQuery<UserEntity> query = em.createQuery("FROM UserEntity WHERE kerberosName in (:krbNames)", UserEntity.class);
            return query.setParameter("krbNames", krbNames).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public RegisteredUser getUserByGitHubName(String gitHubName) {
        return getUser(UserName.GITHUB, gitHubName);
    }

    /**
     * Retrieves users whose github name matches a name in given list.
     *
     * @param names github names we are looking for
     * @return existing users with matching github name
     */
    public Map<Integer, RegisteredUser> getRegisteredUsersByGitHubIds(List<Integer> ids) {
        EntityManager em = entityManagerFactory.createEntityManager();

        try {
            logger.debugf("Retrieving %d users by github names", ids.size());

            final int batchSize = 1000;
            final HashMap<Integer, RegisteredUser> users = new HashMap<>();

            for (int i = 0; i < ids.size(); i = i + batchSize) {
                int highIndex = Math.min(i + batchSize, ids.size());

                logger.debugf("Retrieving indexes %d to %d", i, highIndex);

                List<Integer> sublist = ids.subList(i, highIndex);

                TypedQuery<UserEntity> query = em.createQuery("FROM UserEntity WHERE githubId in (:list)", UserEntity.class);
                List<UserEntity> result = query.setParameter("list", sublist).getResultList();

                for (UserEntity user : result) {
                    users.put(user.getGithubId(), convertUserEntity(user));
                }
            }

            return users;
        } finally {
            em.close();
        }
    }

    private RegisteredUser getUser(UserName userName, String param) {
        EntityManager em = entityManagerFactory.createEntityManager();

        try {
            RegisteredUser user = null;

            TypedQuery<UserEntity> getUserQuery;

            if (userName == UserName.KERBEROS) {
                logger.debug("Retrieving user by krb name");
                getUserQuery = em.createQuery("FROM UserEntity WHERE kerberosName=:name", UserEntity.class);
            } else {
                logger.debug("Retrieving user by github name");
                getUserQuery = em.createQuery("FROM UserEntity WHERE githubName=:name", UserEntity.class);
            }

            List<UserEntity> result = getUserQuery.setParameter("name", param).getResultList();

            if (result.size() == 1) {
                UserEntity userEntity = result.get(0);
                user = convertUserEntity(userEntity);
            }

            return user;
        } finally {
            em.close();
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void registerUser(RegisteredUser user, LdapRepositoryBean.LdapUserRecord ldapUserRecord) {
        UserEntity userEntity = convertUser(user);

        if (StringUtils.isNotBlank(user.getKrbName())) {
            if (ldapUserRecord == null) {
                throw new ApplicationException("No LDAP record found for username " + user.getKrbName());
            }
            userEntity.setEmployeeNumber(ldapUserRecord.getEmployeeNumber());
        }

        insertUser(userEntity);
        user.setId(userEntity.getId());
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RegisteredUser getOrCreateUser(String kerberosName) {
        // Note: "kerberosName" and "ldapName" are synonyms.

        // Given LDAP name can be current or prior employee LDAP name (employees LDAP names can change over time, prior
        // names are recorded in employee's LDAP record). We need to retrieve current and all prior LDAP names of given
        // employee, because an employee can be registered under any of those.
        LdapRepositoryBean.LdapUserRecord ldapRecord = ldapRepository.findUserRecord(kerberosName);
        if (ldapRecord == null) {
            throw new ApplicationException("No LDAP record found for username " + kerberosName);
        }

        // Now load all UserEntity records with LDAP names discovered in previous step.
        List<UserEntity> userEntities = getUsersByKrbNames(ldapRecord.getAllUids());

        RegisteredUser user;
        if (userEntities.size() > 1) {
            // If more than one record has been found, the situation needs to be remedied by an administrator.
            // This can indicate following situations:
            //
            // * The registrations belong to the same employee, in which case the extra registrations need to be removed.
            //   This should not happen as Mjolnir should always reuse a previous employee's registration, even after
            //   a username change, if it exists.
            //
            // * The registrations belong to different employees. Administrator can verify this by checking employee
            //   number fields. It's unknown if this can happen in practice (if Identity Access Management tooling
            //   guards against reuse of past usernames). I tried to verify this with IAM team but the answer was not
            //   conclusive.
            //
            // This risk of these situations happening seems to be reasonably low.
            String discoveredEntityNames = userEntities.stream()
                    .map(UserEntity::getKerberosName)
                    .collect(Collectors.joining(", "));
            throw new ApplicationException(String.format("Several possible registered users found for UID '%s': %s\n" +
                            "Administrator needs to verify that the registrations belong to the same employee and " +
                            "remove the extra registrations.",
                    kerberosName, discoveredEntityNames));
        } else if (userEntities.size() == 1) {
            user = convertUserEntity(userEntities.get(0));
        } else {
            user = new RegisteredUser();
            user.setKrbName(kerberosName);

            UserEntity userEntity = convertUser(user);
            userEntity.setEmployeeNumber(ldapRecord.getEmployeeNumber());
            insertUser(userEntity);
            user.setId(userEntity.getId());
        }

        return user;
    }

    private void insertUser(UserEntity userEntity) {
        if (userEntity.getId() != null) {
            throw new ApplicationException(
                    String.format("Can't insert new user, the entity already has an ID: %s", userEntity));
        }

        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            em.persist(userEntity);
            em.flush();
        } finally {
            em.close();
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void updateUser(RegisteredUser user, @Nullable LdapRepositoryBean.LdapUserRecord ldapUserRecord) {
        EntityManager em = entityManagerFactory.createEntityManager();

        try {
            UserEntity existingUser = getUserFromDB(user, em);

            if (existingUser == null) {
                throw new ApplicationException(String.format("User with ID %d was not found.", user.getId()));
            }

            if (StringUtils.isNotBlank(user.getKrbName()) && !user.getKrbName().equals(existingUser.getKerberosName())) {
                // overriding LDAP name
                throw new ApplicationException("Changing LDAP name is not allowed.");
            } else if (StringUtils.isNotBlank(user.getKrbName()) && StringUtils.isBlank(existingUser.getKerberosName())) {
                // setting LDAP name, while it was previously empty
                Objects.requireNonNull(ldapUserRecord);
                existingUser.setKerberosName(user.getKrbName());
                existingUser.setEmployeeNumber(ldapUserRecord.getEmployeeNumber());
            }
            existingUser.setGithubName(user.getGitHubName());
            existingUser.setGithubId(user.getGitHubId());
            existingUser.setNote(user.getNote());
            existingUser.setAdmin(user.isAdmin());
            existingUser.setWhitelisted(user.isWhitelisted());
            existingUser.setResponsiblePerson(user.getResponsiblePerson());
        } finally {
            em.close();
        }
    }

    @Override
    public List<RegisteredUser> getAllUsers() {
        EntityManager em = entityManagerFactory.createEntityManager();

        try {
            List<UserEntity> entityList = em.createQuery("FROM UserEntity", UserEntity.class).getResultList();
            final List<RegisteredUser> users = new ArrayList<>();

            for (UserEntity entity : entityList) {
                final RegisteredUser user = convertUserEntity(entity);
                users.add(user);
            }

            return users;
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteUser(RegisteredUser user) {
        EntityManager em = entityManagerFactory.createEntityManager();

        try {
            UserEntity userEntity = getUserFromDB(user, em);

            if (userEntity == null) {
                throw new ApplicationException("Couldn't delete user - user not found.");
            }

            em.remove(userEntity);
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteUsers(Collection<RegisteredUser> users) {
        for (RegisteredUser user : users) {
            deleteUser(user);
        }
    }

    private enum UserName {
        KERBEROS,
        GITHUB
    }

    private UserEntity convertUser(RegisteredUser user) {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(user.getId());
        userEntity.setKerberosName(user.getKrbName());
        userEntity.setGithubName(user.getGitHubName());
        userEntity.setGithubId(user.getGitHubId());
        userEntity.setNote(user.getNote());
        userEntity.setAdmin(user.isAdmin());
        userEntity.setWhitelisted(user.isWhitelisted());
        userEntity.setResponsiblePerson((user.getResponsiblePerson()));

        return userEntity;
    }

    private RegisteredUser convertUserEntity(UserEntity userEntity) {
        if (userEntity == null) {
            return null;
        }

        RegisteredUser registeredUser = new RegisteredUser();
        registeredUser.setId(userEntity.getId());
        registeredUser.setKrbName(userEntity.getKerberosName());
        registeredUser.setGitHubName(userEntity.getGithubName());
        registeredUser.setGitHubId(userEntity.getGithubId());
        registeredUser.setNote(userEntity.getNote());
        registeredUser.setAdmin(userEntity.isAdmin());
        registeredUser.setWhitelisted(userEntity.isWhitelisted());
        registeredUser.setResponsiblePerson(userEntity.getResponsiblePerson());

        return registeredUser;
    }

    private static UserEntity getUserFromDB(RegisteredUser user, EntityManager em) {
        Objects.requireNonNull(user);
        Objects.requireNonNull(user.getId(), "Can't retrieve a user, user ID is null: " + user);
        Objects.requireNonNull(em);
        return em.find(UserEntity.class, user.getId());
    }

}
