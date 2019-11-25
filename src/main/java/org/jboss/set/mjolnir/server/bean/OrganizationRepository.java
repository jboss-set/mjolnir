package org.jboss.set.mjolnir.server.bean;

import org.jboss.set.mjolnir.shared.domain.GithubOrganization;

import java.util.List;

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
     */
    List<GithubOrganization> getOrganizations();

}
