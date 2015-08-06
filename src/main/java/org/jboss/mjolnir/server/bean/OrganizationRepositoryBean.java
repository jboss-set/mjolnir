package org.jboss.mjolnir.server.bean;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jboss.mjolnir.authentication.GithubOrganization;
import org.jboss.mjolnir.authentication.GithubTeam;
import org.jboss.mjolnir.server.entities.GithubOrganizationEntity;
import org.jboss.mjolnir.server.entities.GithubTeamEntity;
import org.jboss.mjolnir.server.util.HibernateUtils;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import java.sql.SQLException;
import java.util.*;

/**
 * {@inheritDoc}
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
@Stateless
public class OrganizationRepositoryBean implements OrganizationRepository {

    private SessionFactory sessionFactory;

    @PostConstruct
    public void initBean() {
        sessionFactory = HibernateUtils.getSessionFactory();
    }

    @Override
    public Set<GithubOrganization> getOrganizations() throws SQLException {
        // load organizations into a map
        Session session = sessionFactory.openSession();

        final List<GithubOrganizationEntity> organizationsList =
                session.createCriteria(GithubOrganizationEntity.class).list();

        session.close();

        final Map<Long, GithubOrganization> orgMap = new HashMap<Long, GithubOrganization>();

        for (GithubOrganizationEntity organization : organizationsList) {
            final GithubOrganization org = new GithubOrganization(organization.getName());
            orgMap.put(organization.getId(), org);
        }

        // load teams and add them to organizations
        session = sessionFactory.openSession();

        final List<GithubTeamEntity> teamsList = session.createCriteria(GithubTeamEntity.class).list();

        session.close();

        for (GithubTeamEntity teamEnt : teamsList) {
            final GithubTeam team = new GithubTeam(teamEnt.getName(), teamEnt.getGithubId().intValue());
            final Long orgId = teamEnt.getOrganization().getId();
            final GithubOrganization org = orgMap.get(orgId);
            if (org != null) {
                org.addTeam(team);
            }
        }

        return new HashSet<GithubOrganization>(orgMap.values());
    }

}
