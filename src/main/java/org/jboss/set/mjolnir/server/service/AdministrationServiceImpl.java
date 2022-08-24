package org.jboss.set.mjolnir.server.service;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.hibernate.HibernateException;
import org.jboss.logging.Logger;
import org.jboss.set.mjolnir.client.exception.ApplicationException;
import org.jboss.set.mjolnir.client.service.AdministrationService;
import org.jboss.set.mjolnir.server.bean.ApplicationParameters;
import org.jboss.set.mjolnir.server.bean.GitHubSubscriptionBean;
import org.jboss.set.mjolnir.server.bean.LdapRepository;
import org.jboss.set.mjolnir.server.bean.LdapRepositoryBean;
import org.jboss.set.mjolnir.server.bean.OrganizationRepository;
import org.jboss.set.mjolnir.server.bean.UserRepository;
import org.jboss.set.mjolnir.server.github.ExtendedUserService;
import org.jboss.set.mjolnir.server.service.validation.GitHubNameIsUniqueValidation;
import org.jboss.set.mjolnir.server.service.validation.KrbNameIsUniqueValidation;
import org.jboss.set.mjolnir.server.service.validation.ResponsiblePersonAddedValidation;
import org.jboss.set.mjolnir.server.service.validation.Validator;
import org.jboss.set.mjolnir.shared.domain.EntityUpdateResult;
import org.jboss.set.mjolnir.shared.domain.GithubOrganization;
import org.jboss.set.mjolnir.shared.domain.GithubTeam;
import org.jboss.set.mjolnir.shared.domain.RegisteredUser;
import org.jboss.set.mjolnir.shared.domain.Subscription;
import org.jboss.set.mjolnir.shared.domain.SubscriptionSummary;
import org.jboss.set.mjolnir.shared.domain.ValidationResult;

import javax.ejb.EJB;
import javax.servlet.ServletException;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Performs administration tasks.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class AdministrationServiceImpl extends AbstractAdminRestrictedService implements AdministrationService {

    private static final Logger log = Logger.getLogger(AdministrationServiceImpl.class);

    @EJB
    private ApplicationParameters applicationParameters;

    @EJB
    private UserRepository userRepository;

    @EJB
    private GitHubSubscriptionBean gitHubSubscriptionBean;

    @EJB
    private LdapRepository ldapRepository;

    @EJB
    private OrganizationRepository organizationRepository;

    private Validator<RegisteredUser> editUserValidator;
    private Validator<RegisteredUser> addUserValidator;
    private ExtendedUserService userService;

    @Override
    public void init() throws ServletException {
        super.init();

        String token = applicationParameters.getMandatoryParameter(ApplicationParameters.GITHUB_TOKEN_KEY);

        GitHubClient client = new GitHubClient();
        client.setOAuth2Token(token);
        userService = new ExtendedUserService(client);

        editUserValidator = new Validator<>();
        editUserValidator.addValidation(new KrbNameIsUniqueValidation(userRepository));
        editUserValidator.addValidation(new GitHubNameIsUniqueValidation(userRepository));
        editUserValidator.addValidation(new ResponsiblePersonAddedValidation());

        addUserValidator = new Validator<>();
        addUserValidator.addValidation(new KrbNameIsUniqueValidation(userRepository));
        addUserValidator.addValidation(new GitHubNameIsUniqueValidation(userRepository));
        addUserValidator.addValidation(new ResponsiblePersonAddedValidation());
    }

    @Override
    public List<GithubOrganization> getOrganizations() throws ApplicationException {
        return organizationRepository.getOrganizations();
    }

    @Override
    public List<Subscription> getMembers(GithubOrganization org, GithubTeam team) {
        if (team == null || team.getId() == null) {
            return gitHubSubscriptionBean.getOrganizationSubscriptions(org.getName());
        } else {
            return gitHubSubscriptionBean.getTeamSubscriptions(team.getId());
        }
    }

    public List<SubscriptionSummary> getOrganizationMembers() {
        return gitHubSubscriptionBean.getOrganizationMembers();
    }

    @Override
    public List<Subscription> getRegisteredUsers() {
        return gitHubSubscriptionBean.getRegisteredUsers();
    }

    @Override
    public Boolean checkUserExists(String userName) {
        return ldapRepository.checkUserExists(userName);
    }

    @Override
    public EntityUpdateResult<RegisteredUser> registerUser(RegisteredUser user) {
        try {
            // retrieve GitHub ID
            ValidationResult updateResult = updateGitHubUserId(user);
            if (!updateResult.isOK()) {
                return EntityUpdateResult.validationFailure(updateResult);
            }

            // verify valid LDAP username
            LdapRepositoryBean.LdapUserRecord ldapUserRecord = null;
            if (StringUtils.isNotBlank(user.getKrbName())) {
                ldapUserRecord = ldapRepository.findUserRecord(user.getKrbName());
                if (ldapUserRecord == null) {
                    return ldapUserNotFound(user.getKrbName());
                }
            }

            ValidationResult validationResult = addUserValidator.validate(user);
            if (validationResult.isOK()) {
                userRepository.registerUser(user, ldapUserRecord);
                return EntityUpdateResult.ok(user);
            } else {
                return EntityUpdateResult.validationFailure(validationResult);
            }
        } catch (HibernateException e) {
            throw new ApplicationException(e);
        }
    }

    @Override
    public void deleteUser(RegisteredUser user) {
        try {
            userRepository.deleteUser(user);
        } catch (HibernateException e) {
            throw new ApplicationException(e);
        }
    }

    @Override
    public void deleteUsers(Collection<RegisteredUser> users) {
        try {
            userRepository.deleteUsers(users);
        } catch (HibernateException e) {
            throw new ApplicationException(e);
        }
    }

    @Override
    public EntityUpdateResult<RegisteredUser> editUser(RegisteredUser user) {
        try {
            // retrieve GitHub ID
            ValidationResult updateResult = updateGitHubUserId(user);
            if (!updateResult.isOK()) {
                return EntityUpdateResult.validationFailure(updateResult);
            }

            RegisteredUser existingUserRecord = userRepository.getUser(user.getId());
            if (user.getGitHubName() != null && user.getGitHubName().equals(existingUserRecord.getGitHubName())
                && !user.getGitHubId().equals(existingUserRecord.getGitHubId())) {
                String message = String.format("Can't save changes! Conflicting GH ID: GH username '%s' did not change, but IDs differ (%d != %d)",
                        user.getGitHubName(), existingUserRecord.getGitHubId(), user.getGitHubId());
                return EntityUpdateResult.validationFailure(message);
            }

            // verify valid LDAP username
            LdapRepositoryBean.LdapUserRecord ldapUserRecord = null;
            if (StringUtils.isNotBlank(user.getKrbName())) {
                ldapUserRecord = ldapRepository.findUserRecord(user.getKrbName());
                if (ldapUserRecord == null) {
                    return ldapUserNotFound(user.getKrbName());
                }
            }

            log.infof("Editing user, original data: %s, new data %s", existingUserRecord, user);

            ValidationResult validationResult = editUserValidator.validate(user);

            if (validationResult.isOK()) {
                userRepository.updateUser(user, ldapUserRecord);
                return EntityUpdateResult.ok(user);
            } else {
                return EntityUpdateResult.validationFailure(validationResult);
            }
        } catch (HibernateException e) {
            throw new ApplicationException(e);
        }
    }

    /**
     * @see GitHubSubscriptionBean#getSubscriptions(String)
     */
    @Override
    public List<GithubOrganization> getSubscriptions(String gitHubName) {
        return gitHubSubscriptionBean.getSubscriptions(gitHubName);
    }

    @Override
    public void unsubscribe(Collection<Subscription> subscriptions) throws ApplicationException {
        for (Subscription subscription: subscriptions) {
            gitHubSubscriptionBean.removeFromOrganizations(subscription.getGitHubName());
        }
    }

    /**
     * @see GitHubSubscriptionBean#setSubscriptions(String, java.util.Map)
     */
    @Override
    public void setSubscriptions(String gitHubName, Map<Integer, Boolean> subscriptions) {
        gitHubSubscriptionBean.setSubscriptions(gitHubName, subscriptions);
    }

    @Override
    public Collection<Subscription> whitelist(Collection<Subscription> subscriptions, boolean whitelist) {
        try {
            for (Subscription subscription : subscriptions) {
                RegisteredUser registeredUser = subscription.getRegisteredUser();
                registeredUser.setWhitelisted(whitelist);
                userRepository.updateUser(registeredUser, null);
            }
            return subscriptions;
        } catch (HibernateException e) {
            throw new ApplicationException("Couldn't whitelist users.", e);
        }
    }

    @Override
    public String findCurrentGithubUsername(int githubId) {
        try {
            User user = userService.getUserById(githubId);
            return user.getLogin();
        } catch (IOException e) {
            log.errorf(e, "Can retrieve user %d via GH API.", githubId);
            return null;
        }
    }

    private ValidationResult updateGitHubUserId(RegisteredUser user) {
        ValidationResult validationResult = new ValidationResult();
        try {
            // if GH username is set, verify it exists and retrieve GH user id
            if (StringUtils.isNotBlank(user.getGitHubName())) {
                User ghUser = userService.getUserIfExists(user.getGitHubName());
                if (ghUser == null) {
                    log.infof("GH username '%s' was not found", user.getGitHubName());
                    validationResult.addFailure(String.format("Username '%s' doesn't exist on GitHub",
                            user.getGitHubName()));
                } else {
                    log.infof("GH ID for username '%s' is %d", user.getGitHubName(), ghUser.getId());
                    user.setGitHubId(ghUser.getId());
                }
            } else {
                user.setGitHubId(null);
            }
            return validationResult;
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }


    // setters

    public void setGitHubSubscriptionBean(GitHubSubscriptionBean gitHubSubscriptionBean) {
        this.gitHubSubscriptionBean = gitHubSubscriptionBean;
    }

    public void setApplicationParameters(ApplicationParameters applicationParameters) {
        this.applicationParameters = applicationParameters;
    }

    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private static EntityUpdateResult<RegisteredUser> ldapUserNotFound(String ldapUsername) {
        return EntityUpdateResult.validationFailure(String.format("User '%s' not found in LDAP", ldapUsername));
    }
}
