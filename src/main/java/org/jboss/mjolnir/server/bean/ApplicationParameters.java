package org.jboss.mjolnir.server.bean;

/**
 * Provides access to application configuration.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public interface ApplicationParameters {

    String getParameter(String name);

}
