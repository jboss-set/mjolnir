package org.jboss.set.mjolnir.server.entities;

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

    private String name;

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
}
