/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.set.mjolnir.shared.domain;

import java.io.Serializable;

/**
 * Bean representing a registered user (i.e. a user that has a record in our user database).
 *
 * Both GitHub name and Kerberos name must be unique, because it has to be possible
 * to identify RegisteredUser record by either KRB name or GitHub username.
 */

public class RegisteredUser implements Serializable {

    private String krbName;
    private String gitHubName;
    private String note;
    private String responsiblePerson;
    private boolean admin;
    private boolean whitelisted;
    private boolean loggedIn;

    public String getKrbName() {
        return krbName;
    }

    public void setKrbName(String krb5Name) {
        this.krbName = krb5Name;
    }

    public String getGitHubName() {
        return gitHubName;
    }

    public void setGitHubName(String gitHubName) {
        this.gitHubName = gitHubName;
    }

    public String getResponsiblePerson() {
        return responsiblePerson;
    }

    public void setResponsiblePerson(String responsiblePerson) {
        this.responsiblePerson = responsiblePerson;
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

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public RegisteredUser copy() {
        RegisteredUser copy = new RegisteredUser();
        copy.setKrbName(this.getKrbName());
        copy.setGitHubName(this.getGitHubName());
        copy.setWhitelisted(this.isWhitelisted());
        copy.setAdmin(this.isAdmin());
        copy.setNote(this.getNote());
        copy.setResponsiblePerson(this.getResponsiblePerson());
        return copy;
    }

    public void copyTo(RegisteredUser other) {
        if (other == null) return;

        other.setGitHubName(gitHubName);
        other.setKrbName(krbName);
        other.setNote(note);
        other.setWhitelisted(whitelisted);
        other.setAdmin(admin);
        other.setResponsiblePerson(responsiblePerson);
    }

    @Override
    public String toString() {
        return "{ krb5Name " + krbName + ": githubName " + gitHubName + " }";
    }

    /**
     * User can have a null name, so two entities are equal only when they have the same github name.
     *
     * @param o object to compare to
     * @return are equal?
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RegisteredUser that = (RegisteredUser) o;

        return gitHubName != null && gitHubName.equals(that.gitHubName);
    }

    @Override
    public int hashCode() {
        return gitHubName != null ? gitHubName.hashCode() : 0;
    }
}
