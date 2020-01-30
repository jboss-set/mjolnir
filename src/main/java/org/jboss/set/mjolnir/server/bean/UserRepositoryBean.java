package org.jboss.set.mjolnir.server.bean;

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
import java.util.List;

/**
 * {@inheritDoc}
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
@Stateless
public class UserRepositoryBean implements UserRepository {

    @Inject
    private EntityManagerFactory entityManagerFactory;

    @Override
    public RegisteredUser getUser(String kerberosName) {
        return getUser(UserName.KERBEROS, kerberosName);
    }

    @Override
    public RegisteredUser getUserByGitHubName(String gitHubName) {
        return getUser(UserName.GITHUB, gitHubName);
    }

    private RegisteredUser getUser(UserName userName, String param) {

        RegisteredUser user = null;

        EntityManager em = entityManagerFactory.createEntityManager();
        TypedQuery<UserEntity> getUserQuery = null;

        if (userName == UserName.KERBEROS) {
            getUserQuery = em.createQuery("FROM UserEntity WHERE kerberosName=:name", UserEntity.class);
        } else {
            getUserQuery = em.createQuery("FROM UserEntity WHERE githubName=:name", UserEntity.class);
        }

        List<UserEntity> result = getUserQuery.setParameter("name", param).getResultList();

        if (result.size() == 1) {
            UserEntity userEntity = result.get(0);
            user = convertUserEntity(userEntity);
        }

        em.close();
        return user;
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
        RegisteredUser user = getUser(kerberosName);
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
        em.persist(userEntity);
        em.close();
    }

    private boolean updateUser(RegisteredUser user) {

        EntityManager em = entityManagerFactory.createEntityManager();

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
        em.close();

        return true;
    }

    @Override
    public List<RegisteredUser> getAllUsers() {
        EntityManager em = entityManagerFactory.createEntityManager();

        List<UserEntity> entityList = em.createQuery("FROM UserEntity", UserEntity.class).getResultList();
        final List<RegisteredUser> users = new ArrayList<>();

        for (UserEntity entity : entityList) {
            final RegisteredUser user = convertUserEntity(entity);
            users.add(user);
        }

        return users;
    }

    @Override
    public void deleteUser(RegisteredUser user) {

        EntityManager em = entityManagerFactory.createEntityManager();

        UserEntity userEntity = getUserFromDB(user, em);

        if (userEntity == null) {
            throw new ApplicationException("Couldn't delete user - user not found.");
        }

        em.remove(userEntity);
        em.close();
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

        return userEntity;
    }

    private RegisteredUser convertUserEntity(UserEntity userEntity) {
        RegisteredUser registeredUser = new RegisteredUser();
        registeredUser.setKrbName(userEntity.getKerberosName());
        registeredUser.setGitHubName(userEntity.getGithubName());
        registeredUser.setNote(userEntity.getNote());
        registeredUser.setAdmin(userEntity.isAdmin());
        registeredUser.setWhitelisted(userEntity.isWhitelisted());

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
}
