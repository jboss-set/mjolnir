package org.jboss.mjolnir.server.bean;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.jboss.mjolnir.authentication.KerberosUser;
import org.jboss.mjolnir.client.exception.ApplicationException;
import org.jboss.mjolnir.server.entities.UserEntity;
import org.jboss.mjolnir.server.util.HibernateUtils;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagementType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * {@inheritDoc}
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
@Stateless
@javax.ejb.TransactionManagement(TransactionManagementType.BEAN)
public class UserRepositoryBean implements UserRepository {

    private SessionFactory sessionFactory;

    @PostConstruct
    public void initBean() {
        sessionFactory = HibernateUtils.getSessionFactory();
    }

    @Override
    public KerberosUser getUser(String kerberosName) {
        return getUser(UserName.KERBEROS, kerberosName);
    }

    @Override
    public KerberosUser getUserByGitHubName(String gitHubName) {
        return getUser(UserName.GITHUB, gitHubName);
    }

    private KerberosUser getUser(UserName userName, String param) {

        KerberosUser user = null;

        Session session = sessionFactory.openSession();
        Criteria criteria = session.createCriteria(UserEntity.class);

        switch (userName) {
            case KERBEROS:
                criteria.add(Restrictions.eq("kerberosName", param));
                break;
            case GITHUB:
                criteria.add(Restrictions.eq("githubName", param));
                break;
        }

        UserEntity userEntity = (UserEntity) criteria.uniqueResult();

        if (userEntity != null) {
            user = new KerberosUser();
            user.setName(userEntity.getKerberosName());
            user.setGithubName(userEntity.getGithubName());
            user.setAdmin(userEntity.isAdmin());
            user.setWhitelisted(userEntity.isWhitelisted());
        }

        session.close();
        return user;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void saveUser(KerberosUser user) {
        insertUser(user);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void saveOrUpdateUser(KerberosUser user) {
        if (!updateUser(user)) {
            insertUser(user);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public KerberosUser getOrCreateUser(String kerberosName) {
        KerberosUser user = getUser(kerberosName);
        if (user == null) {
            user = new KerberosUser();
            user.setName(kerberosName);
            insertUser(user);
        }
        return user;
    }

    private void insertUser(KerberosUser user) {
        UserEntity userEntity = convertUser(user);

        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.save(userEntity);
        session.getTransaction().commit();
        session.close();
    }

    private boolean updateUser(KerberosUser user) {
        UserEntity userEntity = getUserFromDB(user);

        if(userEntity == null) {
            //user is not stored in the DB
            return false;
        }

        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.update(userEntity);
        session.getTransaction().commit();
        session.close();

        return true;
    }

    @Override
    public List<KerberosUser> getAllUsers() {
        Session session = sessionFactory.openSession();

        List<UserEntity> entityList = session.createCriteria(UserEntity.class).list();
        final List<KerberosUser> users = new ArrayList<>();

        for(UserEntity entity : entityList) {
            final KerberosUser user = new KerberosUser();
            user.setName(entity.getKerberosName());
            user.setGithubName(entity.getGithubName());
            user.setAdmin(entity.isAdmin());
            user.setWhitelisted(entity.isWhitelisted());
            users.add(user);
        }

        return users;
    }

    @Override
    public void deleteUser(KerberosUser user) {
        UserEntity userEntity = getUserFromDB(user);

        if(user == null) {
            throw new ApplicationException("Couldn't delete user - user not found.");
        }

        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.delete(userEntity);
        session.getTransaction().commit();
        session.close();
    }

    @Override
    public void deleteUsers(Collection<KerberosUser> users) {
        for (KerberosUser user : users) {
            deleteUser(user);
        }
    }

    private enum UserName {
        KERBEROS,
        GITHUB
    }

    private UserEntity convertUser(KerberosUser user) {
        UserEntity userEntity = new UserEntity();
        userEntity.setKerberosName(user.getName());
        userEntity.setGithubName(user.getGithubName());
        userEntity.setAdmin(user.isAdmin());
        userEntity.setWhitelisted(user.isWhitelisted());

        return userEntity;
    }

    private UserEntity getUserFromDB(KerberosUser user) {
        UserEntity userEntity;
        Session session = sessionFactory.openSession();

        //kerberosName is unique in the DB
        userEntity = (UserEntity) session.createCriteria(UserEntity.class)
                .add(Restrictions.eq("kerberosName", user.getName())).uniqueResult();

        //githubName is unique in the DB
        if (userEntity == null) {
            userEntity = (UserEntity) session.createCriteria(UserEntity.class)
                    .add(Restrictions.eq("githubName", user.getGithubName())).uniqueResult();
        }

        userEntity.setKerberosName(user.getName());
        userEntity.setGithubName(user.getGithubName());
        userEntity.setAdmin(user.isAdmin());
        userEntity.setWhitelisted(user.isWhitelisted());

        return userEntity;
    }
}
