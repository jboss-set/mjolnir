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
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper class to hold the team information for each Github organization in the github-team-data xml file.
 *
 * @author: navssurtani
 * @since: 0.1
 */

public class GithubOrganization implements Serializable {

    private String name;
    private List<GithubTeam> teams;
    private String token;

    public GithubOrganization(String name) {
        this.name = name;
        this.teams = new ArrayList<GithubTeam>();
    }

    public GithubOrganization() {
    }

    public void addTeam(GithubTeam t) {
        teams.add(t);
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<GithubTeam> getTeams() {
        return teams;
    }

    public String getName() {
        return name;
    }

    public String getToken() {
        return token;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        sb.append(" Org name: " + name);
        sb.append(" Teams: ");
        for (GithubTeam t : teams){
            sb.append('[');
            sb.append("Team name=" + t.getName() + " Id=" + t.getId());
            sb.append("] ");
        }
        sb.append('}');
        return sb.toString();
    }
}
