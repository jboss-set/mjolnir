package org.jboss.mjolnir.server.bean;

import org.jboss.mjolnir.authentication.GithubOrganization;
import org.jboss.mjolnir.authentication.GithubTeam;
import org.jboss.mjolnir.server.entities.GithubOrganizationEntity;
import org.jboss.mjolnir.server.entities.GithubTeamEntity;
import org.jboss.mjolnir.server.util.JpaUtils;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@inheritDoc}
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
@Stateless
public class OrganizationRepositoryBean implements OrganizationRepository {

    private EntityManagerFactory entityManagerFactory;

    @PostConstruct
    public void initBean() {
        entityManagerFactory = JpaUtils.getEntityManagerFactory();
    }

    @Override
    public Set<GithubOrganization> getOrganizations() throws SQLException {
        // load organizations into a map
        EntityManager em = entityManagerFactory.createEntityManager();

        final List<GithubOrganizationEntity> organizationsList =
                em.createQuery("FROM GithubOrganizationEntity", GithubOrganizationEntity.class).getResultList();

        em.close();

        final Map<Long, GithubOrganization> orgMap = new HashMap<>();

        for (GithubOrganizationEntity organization : organizationsList) {
            final GithubOrganization org = new GithubOrganization(organization.getName());
            orgMap.put(organization.getId(), org);
        }

        // load teams and add them to organizations
        em = entityManagerFactory.createEntityManager();

        final List<GithubTeamEntity> teamsList =
                em.createQuery("FROM GithubTeamEntity", GithubTeamEntity.class).getResultList();

        em.close();

        for (GithubTeamEntity teamEnt : teamsList) {
            final GithubTeam team = new GithubTeam(teamEnt.getName(), teamEnt.getGithubId().intValue());
            final Long orgId = teamEnt.getOrganization().getId();
            final GithubOrganization org = orgMap.get(orgId);
            if (org != null) {
                org.addTeam(team);
            }
        }

        return new HashSet<>(orgMap.values());
    }

}
