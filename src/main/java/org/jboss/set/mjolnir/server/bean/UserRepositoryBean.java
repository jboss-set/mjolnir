package org.jboss.set.mjolnir.server.bean;

import org.jboss.logging.Logger;
import org.jboss.set.mjolnir.shared.domain.RegisteredUser;
import org.jboss.set.mjolnir.client.exception.ApplicationException;
import org.jboss.set.mjolnir.server.entities.UserEntity;

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
    public Map<String, RegisteredUser> getUsersByGitHubName(List<String> names) {
        EntityManager em = entityManagerFactory.createEntityManager();

        try {
            logger.debugf("Retrieving %d users by github names", names.size());

            final int batchSize = 1000;
            final HashMap<String, RegisteredUser> users = new HashMap<>();

            names = toLowerCase(names);
            for (int i = 0; i < names.size(); i = i + batchSize) {
                int highIndex = Math.min(i + batchSize, names.size());

                logger.debugf("Retrieving indexes %d to %d", i, highIndex);

                List<String> sublist = names.subList(i, highIndex);

                TypedQuery<UserEntity> query = em.createQuery("FROM UserEntity WHERE lower(githubName) in (:list)", UserEntity.class);
                List<UserEntity> result = query.setParameter("list", sublist).getResultList();

                for (UserEntity user : result) {
                    users.put(user.getGithubName().toLowerCase(), convertUserEntity(user));
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
    public void saveUser(RegisteredUser user) {
        insertUser(user);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void saveOrUpdateUser(RegisteredUser user) {
        if (!updateUser(user)) {
            insertUser(user);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RegisteredUser getOrCreateUser(String kerberosName) {
        List<String> uids = ldapRepository.findAllUserUids(kerberosName);
        List<UserEntity> userEntities = getUsersByKrbNames(uids);

        if (userEntities.size() > 1) {
            logger.warnf("Several possible registered users found for UID %s", kerberosName);
        }

        RegisteredUser user;
        if (userEntities.size() == 1) {
            user = convertUserEntity(userEntities.get(0));
        } else {
            user = getUser(kerberosName);
        }

        if (user == null) {
            user = new RegisteredUser();
            user.setKrbName(kerberosName);
            insertUser(user);
        }

        return user;
    }

    private void insertUser(RegisteredUser user) {
        UserEntity userEntity = convertUser(user);

        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            em.persist(userEntity);
        } finally {
            em.close();
        }
    }

    private boolean updateUser(RegisteredUser user) {
        EntityManager em = entityManagerFactory.createEntityManager();

        try {
            UserEntity userEntity = getUserFromDB(user, em);

            if (userEntity == null) {
                //user is not stored in the DB
                return false;
            }

            userEntity.setKerberosName(user.getKrbName());
            userEntity.setGithubName(user.getGitHubName());
            userEntity.setNote(user.getNote());
            userEntity.setAdmin(user.isAdmin());
            userEntity.setWhitelisted(user.isWhitelisted());
            userEntity.setResponsiblePerson(user.getResponsiblePerson());

            return true;
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
        userEntity.setKerberosName(user.getKrbName());
        userEntity.setGithubName(user.getGitHubName());
        userEntity.setNote(user.getNote());
        userEntity.setAdmin(user.isAdmin());
        userEntity.setWhitelisted(user.isWhitelisted());
        userEntity.setResponsiblePerson((user.getResponsiblePerson()));

        return userEntity;
    }

    private RegisteredUser convertUserEntity(UserEntity userEntity) {
        RegisteredUser registeredUser = new RegisteredUser();
        registeredUser.setKrbName(userEntity.getKerberosName());
        registeredUser.setGitHubName(userEntity.getGithubName());
        registeredUser.setNote(userEntity.getNote());
        registeredUser.setAdmin(userEntity.isAdmin());
        registeredUser.setWhitelisted(userEntity.isWhitelisted());
        registeredUser.setResponsiblePerson(userEntity.getResponsiblePerson());

        return registeredUser;
    }

    private UserEntity getUserFromDB(RegisteredUser user, EntityManager em) {
        if (user == null) {
            throw new ApplicationException("Cannot retrieve a null user from the DB.");
        }
        if (em == null) {
            throw new ApplicationException("Cannot open a DB connection.");
        }

        List<UserEntity> userEntities;

        //first check GH name because krb name can be null
        //githubName is unique in the DB
        userEntities = em.createQuery("FROM UserEntity WHERE githubName=:name", UserEntity.class)
                .setParameter("name", user.getGitHubName()).getResultList();

        //kerberosName is unique in the DB
        if (userEntities.size() == 0) {
            userEntities = em.createQuery("FROM UserEntity WHERE kerberosName=:name", UserEntity.class)
                    .setParameter("name", user.getKrbName()).getResultList();
        }

        UserEntity userEntity = null;

        if (userEntities.size() == 1) {
            userEntity = userEntities.get(0);
        }

        return userEntity;
    }

    private static List<String> toLowerCase(final List<String> strings) {
        final List<String> result = new ArrayList<>(strings.size());
        for (String str : strings) {
            result.add(str.toLowerCase());
        }
        return result;
    }
}
