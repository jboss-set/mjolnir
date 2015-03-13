package org.jboss.mjolnir.server.bean;

import org.jboss.mjolnir.client.exception.ApplicationException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Singleton;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * {@inheritDoc}
 * <p/>
 * Application parameters are loaded during bean initialization and are held in memory.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
@Singleton
@Remote(ApplicationParametersRemote.class)
@Local(ApplicationParameters.class)
public class ApplicationParametersBean implements ApplicationParameters, ApplicationParametersRemote {

    private final static String READ_SQL = "select param_name, param_value from application_parameters";
    private final static String INSERT_SQL = "insert into application_parameters (param_value, param_name) values (?, ?)";
    private final static String UPDATE_SQL = "update application_parameters set param_value = ? where param_name = ?";

    private DataSource dataSource;

    private Map<String, String> parameters = Collections.synchronizedMap(new HashMap<String, String>());

    @PostConstruct
    public void initBean() {
        try {
            dataSource = JndiUtils.getDataSource();
            reloadParameters();
        } catch (SQLException e) {
            throw new ApplicationException("Couldn't load application configuration.", e);
        }
    }

    @Override
    public void reloadParameters() throws SQLException {
        final Connection connection = dataSource.getConnection();
        try {
            final PreparedStatement statement = connection.prepareStatement(READ_SQL);
            final ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                final String param_name = resultSet.getString("param_name");
                final String param_value = resultSet.getString("param_value");
                parameters.put(param_name, param_value);
            }
            resultSet.close();
        } finally {
            connection.close();
        }
    }

    @Override
    public String getParameter(String name) {
        if (parameters.containsKey(name)) {
            return parameters.get(name);
        }
        return null;
    }

    @Override
    public String getMandatoryParameter(String name) {
        final String value = parameters.get(name);
        if (!isEmpty(value)) {
            return value;
        }
        throw new ApplicationException("Application parameter '" + name
                + "' is not set in database.");
    }

    @Override
    public void setParameter(String name, String value) throws SQLException {
        final Connection connection = dataSource.getConnection();
        try {
            final PreparedStatement statement;

            if (parameters.containsKey(name)) {
                statement = connection.prepareStatement(UPDATE_SQL);
            } else {
                statement = connection.prepareStatement(INSERT_SQL);
            }
            statement.setString(1, value);
            statement.setString(2, name);
            statement.execute();

            parameters.put(name, value);
        } finally {
            connection.close();
        }
    }
}
