package org.jboss.set.mjolnir.server.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * @author Martin Stefanko (mstefank@redhat.com)
 */
@Entity
@Table(name = "github_orgs")
public class GithubOrganizationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_github_orgs")
    @SequenceGenerator(name = "sq_github_orgs", sequenceName = "sq_github_orgs", allocationSize = 1)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "subscriptions_enabled")
    private boolean subscriptionsEnabled;

    public GithubOrganizationEntity() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Should it be possible to subscribe into this organization's teams in the UI?
     *
     * @return true - org will appear in the UI, false org will not appear in the UI.
     */
    public boolean isSubscriptionsEnabled() {
        return subscriptionsEnabled;
    }

    public void setSubscriptionsEnabled(boolean subscriptionsEnabled) {
        this.subscriptionsEnabled = subscriptionsEnabled;
    }
}
