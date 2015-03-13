package org.jboss.mjolnir.server.bean;

import org.jboss.mjolnir.authentication.GithubOrganization;
import org.jboss.mjolnir.authentication.GithubTeam;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * {@inheritDoc}
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
@Stateless
public class OrganizationRepositoryBean implements OrganizationRepository {

    private static String GET_ORGS_QUERY = "select id, name from github_orgs";
    private static String GET_TEAMS_QUERY = "select id, org_id, name, github_id from github_teams";

    private DataSource dataSource;

    @PostConstruct
    public void initBean() {
        dataSource = JndiUtils.getDataSource();
    }

    @Override
    public Set<GithubOrganization> getOrganizations() throws SQLException {
        final Connection connection = dataSource.getConnection();
        try {
            // load organizations into a map
            final PreparedStatement orgsStatement = connection.prepareStatement(GET_ORGS_QUERY);
            final ResultSet orgsResult = orgsStatement.executeQuery();
            final Map<Long, GithubOrganization> orgMap = new HashMap<Long, GithubOrganization>();

            while (orgsResult.next()) {
                final GithubOrganization org = new GithubOrganization(orgsResult.getString("name"));
                orgMap.put(orgsResult.getLong("id"), org);
            }

            // load teams and add them to organizations
            final PreparedStatement teamsStatement = connection.prepareStatement(GET_TEAMS_QUERY);
            final ResultSet teamsResult = teamsStatement.executeQuery();

            while (teamsResult.next()) {
                final GithubTeam team = new GithubTeam(teamsResult.getString("name"), teamsResult.getInt("github_id"));
                final Long orgId = teamsResult.getLong("org_id");
                final GithubOrganization org = orgMap.get(orgId);
                if (org != null) {
                    org.addTeam(team);
                }
            }

            return new HashSet<GithubOrganization>(orgMap.values());
        } finally {
            connection.close();
        }
    }

}
