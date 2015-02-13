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

package org.jboss.mjolnir.server.service;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.jboss.mjolnir.authentication.GithubOrganization;
import org.jboss.mjolnir.authentication.GithubTeam;
import org.jboss.mjolnir.authentication.KerberosUser;
import org.jboss.mjolnir.client.exception.ApplicationException;
import org.jboss.mjolnir.client.exception.GitHubNameAlreadyTakenException;
import org.jboss.mjolnir.client.service.GitHubService;
import org.jboss.mjolnir.server.bean.ApplicationParameters;
import org.jboss.mjolnir.server.bean.OrganizationRepository;
import org.jboss.mjolnir.server.bean.UserRepository;
import org.jboss.mjolnir.server.github.ExtendedTeamService;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * {@inheritDoc}
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class GitHubServiceImpl extends AbstractServiceServlet implements GitHubService {

    @EJB
    private OrganizationRepository organizationRepository;
    @EJB
    private ApplicationParameters applicationParameters;
    @EJB
    private UserRepository userRepository;

    private ExtendedTeamService teamService;

    @Override
    public void init() throws ServletException {
        super.init();

        final String token = applicationParameters.getMandatoryParameter(ApplicationParameters.GITHUB_TOKEN_KEY);

        final GitHubClient client = new GitHubClient();
        client.setOAuth2Token(token);
        teamService = new ExtendedTeamService(client);
    }

    @Override
    public KerberosUser modifyGithubName(String newGithubName) throws GitHubNameAlreadyTakenException {
        try {
            // reload authenticated user form database
            final String krb5Name = getAuthenticatedUser().getName();
            final KerberosUser user = userRepository.getUser(krb5Name);
            setAuthenticatedUser(user); // update session with current instance

            final KerberosUser userByGitHubName = userRepository.getUserByGitHubName(newGithubName);
            if (userByGitHubName != null && !userByGitHubName.equals(user)) {
                throw new GitHubNameAlreadyTakenException("This GitHub name is already taken by different user.");
            }

            // update github name
            log("Changing githubName for KerberosUser " + krb5Name + ". Old name is " + user.getGithubName() + ". New name " +
                    "is " + newGithubName);
            user.setGithubName(newGithubName);
            // Now put it back into the cache.
            userRepository.saveUser(user);
            log("Successfully modified GithubName for KerberosUser " + krb5Name);
            return user;
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }

    @Override
    public String subscribe(int teamId) {
        final String githubName = getCurrentUserGitHubName();
        try {
            final String state = teamService.addMembership(teamId, githubName);
            log("Successfully added " + githubName + " to team.");
            return state;
        } catch (IOException e) {
            throw new ApplicationException("Unable to subscribe user " + githubName
                    + " to team #" + teamId, e);
        }
    }

    @Override
    public void unsubscribe(int teamId) {
        final String githubName = getCurrentUserGitHubName();
        try {
            teamService.removeMembership(teamId, githubName);
            log("Successfully removed " + githubName + " from team.");
        } catch (IOException e) {
            throw new ApplicationException("Unable to unsubscribe user " + githubName
                    + " to team #" + teamId, e);
        }
    }

    /**
     * Returns managed organizations and their teams.
     *
     * @return organizations
     */
    @Override
    public Set<GithubOrganization> getAvailableOrganizations() {
        try {
            final Set<GithubOrganization> organizations = organizationRepository.getOrganizations();
            return new HashSet<GithubOrganization>(organizations);
        } catch (SQLException e) {
            throw new ApplicationException("Couldn't load GitHub organizations: " + e.getMessage(), e);
        }
    }

    /**
     * Returns managed organizations and their teams together with information whether the current user
     * is subscribed to given team.
     *
     * @return organizations
     */
    @Override
    public Set<GithubOrganization> getSubscriptions() {
        try {
            final Set<GithubOrganization> organizations = getAvailableOrganizations();
            final String gitHubName = getCurrentUserGitHubName();

            for (GithubOrganization organization : organizations) {
                for (GithubTeam team : organization.getTeams()) {
                    final String membershipState = teamService.getMembership(team.getId(), gitHubName);
                    team.setMembershipState(membershipState);
                }
            }
            return organizations;
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }

    private String getCurrentUserGitHubName() {
        final KerberosUser user = getAuthenticatedUser();
        final String gitHubName = user.getGithubName();
        if (gitHubName == null) {
            throw new ApplicationException("Operation failed, user must set GitHub name first.");
        }
        return gitHubName;
    }

    @Override
    protected boolean performAuthorization() {
        // user must be authenticated
        final KerberosUser loggedUser = getAuthenticatedUser();
        return loggedUser != null;
    }

}
