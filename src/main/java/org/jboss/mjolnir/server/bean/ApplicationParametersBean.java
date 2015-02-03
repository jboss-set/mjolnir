package org.jboss.mjolnir.server.bean;

import org.jboss.mjolnir.client.exception.ApplicationException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Singleton;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * {@inheritDoc}
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
@Singleton
@Remote(ApplicationParametersRemote.class)
@Local(ApplicationParameters.class)
public class ApplicationParametersBean implements ApplicationParameters, ApplicationParametersRemote {

    private final static String READ_PARAMS_SQL = "select param_name, param_value from application_parameters";

    @Resource(lookup = "java:jboss/datasources/MjolnirDS")
    private DataSource dataSource;

    private Map<String, String> parameters = new HashMap<String, String>();

    @PostConstruct
    public void initBean() throws SQLException {
        reloadParameters();
    }

    @Override
    public void reloadParameters() throws SQLException {
        final Connection connection = dataSource.getConnection();
        try {
            final PreparedStatement statement = connection.prepareStatement(READ_PARAMS_SQL);
            final ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                final String param_name = resultSet.getString("param_name");
                final String param_value = resultSet.getString("param_value");
                parameters.put(param_name, param_value);
            }
        } finally {
            connection.close();
        }
    }

    @Override
    public String getParameter(String name) {
        final String value = parameters.get(name);
        if (!isEmpty(value)) {
            return value;
        }
        throw new ApplicationException("Application parameter '" + name
                + "' is not set in database.");
    }
}
