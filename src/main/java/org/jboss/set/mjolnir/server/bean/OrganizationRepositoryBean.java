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
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            // load organizations into a map
            final List<GithubOrganizationEntity> organizationsList =
                    em.createQuery("FROM GithubOrganizationEntity WHERE subscriptionsEnabled = true", GithubOrganizationEntity.class)
                            .getResultList();


            final Map<Long, GithubOrganization> orgMap = new HashMap<>();

            for (GithubOrganizationEntity organization : organizationsList) {
                final GithubOrganization org = new GithubOrganization(organization.getName());
                orgMap.put(organization.getId(), org);
            }

            // load teams and add them to organizations
            final List<GithubTeamEntity> teamsList =
                    em.createQuery("FROM GithubTeamEntity", GithubTeamEntity.class).getResultList();

            for (GithubTeamEntity teamEnt : teamsList) {
                final GithubTeam team = new GithubTeam(teamEnt.getName(), teamEnt.getGithubId().intValue(),
                        Boolean.TRUE.equals(teamEnt.getSelfService()));
                final Long orgId = teamEnt.getOrganization().getId();
                final GithubOrganization org = orgMap.get(orgId);
                if (org != null) {
                    org.addTeam(team);
                }
            }

            return new ArrayList<>(orgMap.values());
        } finally {
            em.close();
        }
    }

    @Override
    public GithubOrganization getOrganization(String name) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            GithubOrganizationEntity e = em.createQuery("FROM GithubOrganizationEntity WHERE name = ?1", GithubOrganizationEntity.class)
                    .setParameter(1, name)
                    .getSingleResult();
            if (e == null) {
                return null;
            }
            return new GithubOrganization(e.getName());
        } finally {
            em.close();
        }
    }

    @Override
    public GithubTeam getTeamByGithubId(long githubId) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            GithubTeamEntity e = em.createQuery("FROM GithubTeamEntity WHERE githubId = ?1", GithubTeamEntity.class)
                    .setParameter(1, githubId)
                    .getSingleResult();
            if (e == null) {
                return null;
            }
            return new GithubTeam(e.getName(), e.getId().intValue(), Boolean.TRUE.equals(e.getSelfService()));
        } finally {
            em.close();
        }
    }

}
