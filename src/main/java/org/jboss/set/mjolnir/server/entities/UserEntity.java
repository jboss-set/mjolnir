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
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_users")
    @SequenceGenerator(name = "sq_users", sequenceName = "sq_users", allocationSize = 1)
    private Long id;

    @Column(name = "krb_name", unique = true)
    private String kerberosName;

    @Column(name = "employee_number", unique = true)
    private Integer employeeNumber;

    @Column(name = "github_name", unique = true)
    private String githubName;

    @Column(name = "github_id", unique = true)
    private Integer githubId;

    @Column(name = "responsible_person")
    private String responsiblePerson;

    @Column
    private String note;

    private boolean admin;

    private boolean whitelisted;

    public UserEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKerberosName() {
        return kerberosName;
    }

    public void setKerberosName(String kerberosName) {
        this.kerberosName = kerberosName;
    }

    public String getGithubName() {
        return githubName;
    }

    public void setGithubName(String githubName) {
        this.githubName = githubName;
    }

    public Integer getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(Integer employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public Integer getGithubId() {
        return githubId;
    }

    public void setGithubId(Integer githubId) {
        this.githubId = githubId;
    }

    public String getNote() {
        return note;
    }

    public void setResponsiblePerson(String responsiblePerson) {
        this.responsiblePerson = responsiblePerson;
    }

    public String getResponsiblePerson() {
        return responsiblePerson;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isWhitelisted() {
        return whitelisted;
    }

    public void setWhitelisted(boolean whitelisted) {
        this.whitelisted = whitelisted;
    }
}
