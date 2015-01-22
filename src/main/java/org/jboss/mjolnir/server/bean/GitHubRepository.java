package org.jboss.mjolnir.server.bean;

import org.jboss.mjolnir.authentication.GithubOrganization;

import java.sql.SQLException;
import java.util.Collection;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public interface GitHubRepository {

    Collection<GithubOrganization> getOrganizations() throws SQLException;

}
