package org.jboss.set.mjolnir.server.bean;

import org.jboss.set.mjolnir.server.entities.GithubOrganizationEntity;
import org.jboss.set.mjolnir.server.entities.GithubTeamEntity;
import org.jboss.set.mjolnir.shared.domain.GithubOrganization;
import org.jboss.set.mjolnir.shared.domain.GithubTeam;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@inheritDoc}
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
@Stateless
public class OrganizationRepositoryBean implements OrganizationRepository {

    @Inject
    private EntityManagerFactory entityManagerFactory;

    @Override
    public List<GithubOrganization> getOrganizations() {
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

        return new ArrayList<>(orgMap.values());
    }

}
