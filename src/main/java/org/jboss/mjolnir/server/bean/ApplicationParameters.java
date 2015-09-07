package org.jboss.mjolnir.server.bean;

import java.sql.SQLException;

/**
 * Provides access to application configuration.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public interface ApplicationParameters {

    String GITHUB_TOKEN_KEY = "github.token";

    String LDAP_URL_KEY = "ldap.url";

    /**
     * Retrieves parameter value.
     *
     * @param name parameter name
     * @return value or null if not set
     */
    String getParameter(String name);

    /**
     * Retrieves parameter value. If value is not set, throws ApplicationException.
     *
     * @param name parameter name
     * @return value
     */
    String getMandatoryParameter(String name);

    /**
     * Set parameter value.
     *
     * @param name parameter name
     * @param value parameter value
     */
    void setParameter(String name, String value) throws SQLException;

}
