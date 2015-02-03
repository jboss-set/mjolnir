package org.jboss.mjolnir.server.bean;

/**
 * Provides access to application configuration.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public interface ApplicationParameters {

    public final static String GITHUB_TOKEN_KEY = "github.token";

    public final static String LDAP_URL_KEY = "ldap.url";


    String getParameter(String name);

}
