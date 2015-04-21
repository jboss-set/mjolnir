package org.jboss.mjolnir.server.bean;

import org.jboss.mjolnir.authentication.KerberosUser;
import org.jboss.mjolnir.client.exception.ApplicationException;
import org.jboss.mjolnir.server.util.JndiUtils;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    private final static String GET_USER_SQL = "select id, krb_name, github_name, admin, whitelisted from users where krb_name = ?";
    private final static String DELETE_USER_SQL = "delete from users where krb_name = ? or (krb_name is null and github_name = ?)";
    private final static String GET_USER_BY_GITHUB_NAME_SQL = "select id, krb_name, github_name, admin, whitelisted from users where github_name = ?";
    private final static String UPDATE_USER_SQL = "update users set krb_name = ?, github_name = ?, whitelisted = ? where krb_name = ? or (krb_name is null and github_name = ?)";
    private final static String INSERT_USER_SQL = "insert into users (github_name, krb_name, whitelisted) values (?, ?, ?)";
    private final static String GET_ALL_USERS_SQL = "select id, krb_name, github_name, admin, whitelisted from users order by krb_name";

    private DataSource dataSource;

    @PostConstruct
    public void initBean() {
        dataSource = JndiUtils.getDataSource();
    }

    @Override
    public KerberosUser getUser(String kerberosName) throws SQLException {
        return getUser(GET_USER_SQL, kerberosName);
    }

    @Override
    public KerberosUser getUserByGitHubName(String gitHubName) throws SQLException {
        return getUser(GET_USER_BY_GITHUB_NAME_SQL, gitHubName);
    }

    private KerberosUser getUser(String sql, String param) throws SQLException {
        final Connection connection = dataSource.getConnection();
        try {
            final PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, param);
            final ResultSet resultSet = statement.executeQuery();

            KerberosUser user = null;
            if (resultSet.next()) {
                user = new KerberosUser();
                user.setName(resultSet.getString("krb_name"));
                user.setGithubName(resultSet.getString("github_name"));
                user.setAdmin(resultSet.getBoolean("admin"));
                user.setWhitelisted(resultSet.getBoolean("whitelisted"));
            }
            resultSet.close();
            statement.close();
            return user;
        } finally {
            connection.close();
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void saveUser(KerberosUser user) throws SQLException {
        if (updateUser(user) == 0) {
            insertUser(user);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public KerberosUser getOrCreateUser(String kerberosName) throws SQLException {
        KerberosUser user = getUser(kerberosName);
        if (user == null) {
            user = new KerberosUser();
            user.setName(kerberosName);
            insertUser(user);
        }
        return user;
    }

    private void insertUser(KerberosUser user) throws SQLException {
        final Connection connection = dataSource.getConnection();
        try {
            PreparedStatement statement;
            statement = connection.prepareStatement(INSERT_USER_SQL);
            statement.setString(1, user.getGithubName());
            statement.setString(2, user.getName());
            statement.setBoolean(3, user.isWhitelisted());
            statement.executeUpdate();
            statement.close();
        } finally {
            connection.close();
        }
    }

    private int updateUser(KerberosUser user) throws SQLException {
        final Connection connection = dataSource.getConnection();
        try {
            PreparedStatement statement;
            statement = connection.prepareStatement(UPDATE_USER_SQL);
            statement.setString(1, user.getName());
            statement.setString(2, user.getGithubName());
            statement.setBoolean(3, user.isWhitelisted());
            statement.setString(4, user.getName());
            statement.setString(5, user.getGithubName());
            final int affectedRows = statement.executeUpdate();
            statement.close();
            return affectedRows;
        } finally {
            connection.close();
        }
    }

    @Override
    public List<KerberosUser> getAllUsers() throws SQLException {
        final Connection connection = dataSource.getConnection();
        try {
            final PreparedStatement statement = connection.prepareStatement(GET_ALL_USERS_SQL);
            final ResultSet resultSet = statement.executeQuery();

            final List<KerberosUser> users = new ArrayList<KerberosUser>();
            while (resultSet.next()) {
                final KerberosUser user = new KerberosUser();
                user.setName(resultSet.getString("krb_name"));
                user.setGithubName(resultSet.getString("github_name"));
                user.setAdmin(resultSet.getBoolean("admin"));
                user.setWhitelisted(resultSet.getBoolean("whitelisted"));
                users.add(user);
            }
            resultSet.close();
            statement.close();
            return users;
        } finally {
            connection.close();
        }
    }

    @Override
    public void deleteUser(KerberosUser user) throws SQLException {
        final Connection connection = dataSource.getConnection();
        try {
            final PreparedStatement statement = connection.prepareStatement(DELETE_USER_SQL);
            statement.setString(1, user.getName());
            statement.setString(2, user.getGithubName());
            int affectedRecords = statement.executeUpdate();
            if (affectedRecords != 1) {
                throw new ApplicationException("Couldn't delete user - user not found.");
            }
        } finally {
            connection.close();
        }
    }

    @Override
    public void deleteUsers(Collection<KerberosUser> users) throws SQLException {
        for (KerberosUser user: users) {
            deleteUser(user);
        }
    }

}
