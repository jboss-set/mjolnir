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

package org.jboss.mjolnir;

import org.eclipse.egit.github.core.Team;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.TeamService;
import org.jboss.mjolnir.authentication.GithubOrganization;
import org.jboss.mjolnir.server.GithubParser;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Main class to be used for this project. In time, we might not need a Main class.
 *
 * @author: navssurtani
 * @since: 0.1
 */
public class Main {

    private static final String XML_DATA = "/github-team-data.xml";

    public static void main(String[] args) {
        GithubParser parser = GithubParser.getInstance();
        Set<GithubOrganization> organizations = parser.parse(XML_DATA);
        GitHubClient client = new GitHubClient();
        client.setCredentials("navssurtani",null);
        TeamService teamService = new TeamService(client);

        for (GithubOrganization o : organizations) {
            List<Team> teams = null;
            try {
                teams = teamService.getTeams(o.getName());
                for (Team t : teams) {
                    System.out.println("Team: " + t.getName());
                    System.out.println("Team id: " + t.getId());
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

    }


}
