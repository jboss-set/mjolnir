package org.jboss.mjolnir.server.bean;

import org.jboss.mjolnir.authentication.KerberosUser;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * {@inheritDoc}
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
@Stateless
public class UserRepositoryBean implements UserRepository {

    private final static String GET_USER_SQL = "select id, krb_name, github_name, admin from users where krb_name = ?";
    private final static String GET_USER_BY_GITHUB_NAME_SQL = "select id, krb_name, github_name, admin from users where github_name = ?";
    private final static String UPDATE_USER_SQL = "update users set github_name = ? where krb_name = ?";
    private final static String INSERT_USER_SQL = "insert into users (github_name, krb_name) values (?, ?)";

    @Resource(lookup = "java:jboss/datasources/MjolnirDS")
    private DataSource dataSource;

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
    public void updateUser(KerberosUser kerberosUser) throws SQLException {
        final Connection connection = dataSource.getConnection();
        try {
            PreparedStatement statement;
            statement = connection.prepareStatement(UPDATE_USER_SQL);
            statement.setString(1, kerberosUser.getGithubName());
            statement.setString(2, kerberosUser.getName());
            statement.executeUpdate();
            statement.close();
        } finally {
            connection.close();
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public KerberosUser getOrCreateUser(String kerberosName) throws SQLException {
        KerberosUser user = getUser(kerberosName);
        if (user == null) {
            final Connection connection = dataSource.getConnection();

            try {
                PreparedStatement statement;
                statement = connection.prepareStatement(INSERT_USER_SQL);
                statement.setString(1, null);
                statement.setString(2, kerberosName);
                statement.executeUpdate();
                statement.close();
            } finally {
                connection.close();
            }

            user = new KerberosUser();
            user.setName(kerberosName);
        }
        return user;
    }
}
