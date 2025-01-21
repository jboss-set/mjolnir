package org.jboss.set.mjolnir.server.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * @author Martin Stefanko (mstefank@redhat.com)
 */
@Entity
@Table(name = "github_teams")
public class GithubTeamEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_github_teams")
    @SequenceGenerator(name = "sq_github_teams", sequenceName = "sq_github_teams", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name="org_id")
    private GithubOrganizationEntity organization;

    private String name;

    @Column(name = "github_id", unique = true)
    private Long githubId;

    @Column(name = "selfservice")
    private Boolean selfService;

    public GithubTeamEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GithubOrganizationEntity getOrganization() {
        return organization;
    }

    public void setOrganization(GithubOrganizationEntity organization) {
        this.organization = organization;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getGithubId() {
        return githubId;
    }

    public void setGithubId(Long githubId) {
        this.githubId = githubId;
    }

    public Boolean getSelfService() {
        return selfService;
    }
}
