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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.hibernate.HibernateException;
import org.jboss.logging.Logger;
import org.jboss.set.mjolnir.client.exception.ApplicationException;
import org.jboss.set.mjolnir.client.service.GitHubService;
import org.jboss.set.mjolnir.server.bean.OrganizationRepository;
import org.jboss.set.mjolnir.server.bean.UserRepository;
import org.jboss.set.mjolnir.server.github.ExtendedTeamService;
import org.jboss.set.mjolnir.server.github.ExtendedUserService;
import org.jboss.set.mjolnir.server.service.validation.GitHubNameIsUniqueValidation;
import org.jboss.set.mjolnir.server.service.validation.Validator;
import org.jboss.set.mjolnir.shared.domain.EntityUpdateResult;
import org.jboss.set.mjolnir.shared.domain.GithubOrganization;
import org.jboss.set.mjolnir.shared.domain.GithubTeam;
import org.jboss.set.mjolnir.shared.domain.RegisteredUser;
import org.jboss.set.mjolnir.shared.domain.ValidationResult;

import javax.inject.Inject;
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

    private static final Logger logger = Logger.getLogger(GitHubServiceImpl.class);

    @Inject
    OrganizationRepository organizationRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    GitHubClient gitHubClient;

    private ExtendedTeamService teamService;
    private ExtendedUserService userService;

    private Validator<RegisteredUser> validator;

    @Override
    public void init() throws ServletException {
        super.init();

        teamService = new ExtendedTeamService(gitHubClient);
        userService = new ExtendedUserService(gitHubClient);

        validator = new Validator<>();
        validator.addValidation(new GitHubNameIsUniqueValidation(userRepository));
    }

    @Override
    public EntityUpdateResult<RegisteredUser> modifyGitHubName(String newGithubName) {
        if (StringUtils.isBlank(newGithubName)) {
            return EntityUpdateResult.validationFailure("Github name must be non blank.");
        }
        newGithubName = newGithubName.strip();

        try {
            // reload authenticated user form database
            RegisteredUser authenticatedUser = getAuthenticatedUser();
            final RegisteredUser user = userRepository.getUser(authenticatedUser.getKrbName());

            logger.infof("Changing githubName for user %s from %s to %s.",
                    user.getKrbName(), user.getGitHubName(), newGithubName);

            User githubUser = userService.getUserIfExists(newGithubName);
            if (githubUser == null) {
                logger.warnf("Username '%s' doesn't exist on GitHub", newGithubName);
                return EntityUpdateResult.validationFailure(String.format("Username '%s' doesn't exist on GitHub", newGithubName));
            }

            user.setGitHubName(newGithubName);
            user.setGitHubId(githubUser.getId());
            authenticatedUser.setGitHubName(newGithubName);
            ValidationResult validationResult = validator.validate(user);
            if (validationResult.isOK()) {
                userRepository.updateUser(user, null);
                logger.infof("Successfully modified githubName for user %s", user.getKrbName());
                return EntityUpdateResult.ok(user);
            } else {
                logger.warnf("Validation failure: %s", validationResult);
                return EntityUpdateResult.validationFailure(validationResult);
            }
        } catch (HibernateException e) {
            throw new ApplicationException(e);
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }

    @Override
    public String subscribe(int teamId) {
        GithubTeam team = organizationRepository.getTeamByGithubId(teamId);
        if (team == null || !team.isSelfService()) {
            throw new ApplicationException("Users not allowed to manage subscription to this team: " + teamId);
        }
        final String githubName = getCurrentUserGitHubName();
        try {
            final String state = teamService.addMembership(teamId, githubName);
            logger.infof("Successfully added %s to team %d.", githubName, teamId);
            return state;
        } catch (IOException e) {
            final String message = "Unable to subscribe user " + githubName + " to team #" + teamId + ": " + e.getMessage();
            logger.warnf(message, e);
            throw new ApplicationException(message, e);
        }
    }

    @Override
    public void unsubscribe(int teamId) {
        GithubTeam team = organizationRepository.getTeamByGithubId(teamId);
        if (team == null || !team.isSelfService()) {
            throw new ApplicationException("Users not allowed to manage subscription to this team: " + teamId);
        }
        final String githubName = getCurrentUserGitHubName();
        try {
            teamService.removeMembership(teamId, githubName);
            logger.infof("Successfully removed %s from team.", githubName);
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
        RegisteredUser user = getAuthenticatedUser();

        // reload user record from database to consume eventual change of GH username
        user = userRepository.getUser(user.getId());

        Integer gitHubId = user.getGitHubId();
        if (gitHubId == null) {
            throw new ApplicationException("Operation failed, user GH ID not set.");
        }
        try {
            User githubUser = userService.getUserById(gitHubId);
            return githubUser.getLogin();
        } catch (IOException e) {
            throw new ApplicationException("Unable to retrieve GH user by his ID: " + gitHubId, e);
        }
    }

    @Override
    protected boolean performAuthorization() {
        // user must be authenticated
        final RegisteredUser loggedUser = getAuthenticatedUser();
        return loggedUser != null;
    }

}
