package org.jboss.mjolnir.server.bean;

import org.jboss.mjolnir.authentication.GithubOrganization;

import java.sql.SQLException;
import java.util.Set;

/**
 * Provides access to configured GitHub organizations and teams.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public interface OrganizationRepository {

    /**
     * Retrieves configured organizations and their teams.
     *
     * @return organizations
     * @throws SQLException
     */
    Set<GithubOrganization> getOrganizations() throws SQLException;

}
