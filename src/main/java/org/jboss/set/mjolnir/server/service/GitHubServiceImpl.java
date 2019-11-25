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

package org.jboss.set.mjolnir.server.service;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.UserService;
import org.hibernate.HibernateException;
import org.jboss.set.mjolnir.client.exception.ApplicationException;
import org.jboss.set.mjolnir.client.service.GitHubService;
import org.jboss.set.mjolnir.server.bean.ApplicationParameters;
import org.jboss.set.mjolnir.server.bean.OrganizationRepository;
import org.jboss.set.mjolnir.server.bean.UserRepository;
import org.jboss.set.mjolnir.server.github.ExtendedTeamService;
import org.jboss.set.mjolnir.server.service.validation.GitHubNameExistsValidation;
import org.jboss.set.mjolnir.server.service.validation.GitHubNameTakenValidation;
import org.jboss.set.mjolnir.server.service.validation.Validator;
import org.jboss.set.mjolnir.shared.domain.EntityUpdateResult;
import org.jboss.set.mjolnir.shared.domain.GithubOrganization;
import org.jboss.set.mjolnir.shared.domain.GithubTeam;
import org.jboss.set.mjolnir.shared.domain.KerberosUser;
import org.jboss.set.mjolnir.shared.domain.ValidationResult;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    private Validator<KerberosUser> validator;

    @Override
    public void init() throws ServletException {
        super.init();

        String token = applicationParameters.getMandatoryParameter(ApplicationParameters.GITHUB_TOKEN_KEY);

        GitHubClient client = new GitHubClient();
        client.setOAuth2Token(token);
        teamService = new ExtendedTeamService(client);
        UserService userService = new UserService(client);

        validator = new Validator<>();
        validator.addValidation(new GitHubNameTakenValidation(userRepository));
        validator.addValidation(new GitHubNameExistsValidation(userService));
    }

    @Override
    public EntityUpdateResult<KerberosUser> modifyGitHubName(String newGithubName) {
        try {
            // reload authenticated user form database
            final String krb5Name = getAuthenticatedUser().getName();
            final KerberosUser user = userRepository.getUser(krb5Name);

            log(String.format("Changing githubName for user %s from %s to %s.",
                    krb5Name, user.getGithubName(), newGithubName));

            user.setGithubName(newGithubName);
            ValidationResult validationResult = validator.validate(user);
            if (validationResult.isOK()) {
                userRepository.saveOrUpdateUser(user);
                setAuthenticatedUser(user); // update session with current instance
                log(String.format("Successfully modified githubName for user %s", krb5Name));
                return EntityUpdateResult.ok(user);
            } else {
                log(String.format("Validation failure: %s", validationResult));
                return EntityUpdateResult.validationFailure(validationResult);
            }
        } catch (HibernateException e) {
            throw new ApplicationException(e);
        }
    }

    @Override
    public String subscribe(int teamId) {
        final String githubName = getCurrentUserGitHubName();
        try {
            final String state = teamService.addMembership(teamId, githubName);
            log("Successfully added " + githubName + " to team.");            return state;
        } catch (IOException e) {
            final String message = "Unable to subscribe user " + githubName + " to team #" + teamId + ": " + e.getMessage();
            log(message, e);
            throw new ApplicationException(message, e);
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
    public List<GithubOrganization> getAvailableOrganizations() {
        final List<GithubOrganization> organizations = organizationRepository.getOrganizations();
        return new ArrayList<>(organizations);
    }

    /**
     * Returns managed organizations and their teams together with information whether the current user
     * is subscribed to given team.
     *
     * @return organizations
     */
    @Override
    public List<GithubOrganization> getSubscriptions() {
        try {
            final List<GithubOrganization> organizations = getAvailableOrganizations();
            final String gitHubName = getCurrentUserGitHubName();

            for (GithubOrganization organization : organizations) {
                for (GithubTeam team : organization.getTeams()) {
                    final String membershipState = teamService.getMembership(team.getId(), gitHubName);
                    team.setMembershipState(membershipState);
                }
            }
            return organizations;
        } catch (IOException e) {
            throw new ApplicationException("Can't obtain membership information from GH API.", e);
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
