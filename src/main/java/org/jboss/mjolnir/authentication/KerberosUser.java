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

package org.jboss.mjolnir.authentication;

import java.io.Serializable;

/**
 * User bean
 *
 * @author: navssurtani
 * @since: 0.1
 */

public class KerberosUser implements Serializable {

    private String krb5Name;
    private String githubName;
    private boolean admin;
    private boolean whitelisted;

    public String getName() {
        return krb5Name;
    }

    public void setName(String krb5Name) {
        this.krb5Name = krb5Name;
    }

    public String getGithubName() {
        return githubName;
    }

    public void setGithubName(String githubName) {
        this.githubName = githubName;
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

    public KerberosUser copy() {
        KerberosUser copy = new KerberosUser();
        copy.setName(this.getName());
        copy.setGithubName(this.getGithubName());
        copy.setWhitelisted(this.isWhitelisted());
        return copy;
    }

    @Override
    public String toString() {
        return "{ krb5Name " + krb5Name + ": githubName " + githubName + " }";
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

        KerberosUser that = (KerberosUser) o;

        return githubName.equals(that.githubName);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (githubName != null ? githubName.hashCode() : 0);
        return result;
    }
}
