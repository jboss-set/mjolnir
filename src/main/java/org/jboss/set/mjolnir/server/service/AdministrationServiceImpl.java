package org.jboss.set.mjolnir.server.service;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.UserService;
import org.hibernate.HibernateException;
import org.jboss.set.mjolnir.client.exception.ApplicationException;
import org.jboss.set.mjolnir.client.service.AdministrationService;
import org.jboss.set.mjolnir.server.bean.ApplicationParameters;
import org.jboss.set.mjolnir.server.bean.GitHubSubscriptionBean;
import org.jboss.set.mjolnir.server.bean.LdapRepository;
import org.jboss.set.mjolnir.server.bean.OrganizationRepository;
import org.jboss.set.mjolnir.server.bean.UserRepository;
import org.jboss.set.mjolnir.server.service.validation.GitHubNameExistsValidation;
import org.jboss.set.mjolnir.server.service.validation.GitHubNameRegisteredValidation;
import org.jboss.set.mjolnir.server.service.validation.KrbNameTakenValidation;
import org.jboss.set.mjolnir.server.service.validation.ResponsiblePersonAddedValidation;
import org.jboss.set.mjolnir.server.service.validation.Validation;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Performs administration tasks.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class AdministrationServiceImpl extends AbstractAdminRestrictedService implements AdministrationService {


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

    @Override
    public void init() throws ServletException {
        super.init();

        String token = applicationParameters.getMandatoryParameter(ApplicationParameters.GITHUB_TOKEN_KEY);

        GitHubClient client = new GitHubClient();
        client.setOAuth2Token(token);
        UserService userService = new UserService(client);


        Validation<RegisteredUser> krbNameTakenValidation = new KrbNameTakenValidation(userRepository);

        editUserValidator = new Validator<>();
        editUserValidator.addValidation(krbNameTakenValidation);
        editUserValidator.addValidation(new ResponsiblePersonAddedValidation());

        addUserValidator = new Validator<>();
        addUserValidator.addValidation(krbNameTakenValidation);
        addUserValidator.addValidation(new GitHubNameRegisteredValidation(userRepository));
        addUserValidator.addValidation(new GitHubNameExistsValidation(userService));
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

    /**
     * @see GitHubSubscriptionBean#getOrganizationMembers()
     */
    public List<SubscriptionSummary> getOrganizationMembers() {
        return gitHubSubscriptionBean.getOrganizationMembers();
    }

    /**
     * @see GitHubSubscriptionBean#getRegisteredUsers()
     */
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
            ValidationResult validationResult = addUserValidator.validate(user);

            if (validationResult.isOK()) {
                userRepository.saveUser(user);
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
    public EntityUpdateResult<RegisteredUser> editUser(RegisteredUser user, boolean validateKrbName, boolean validateGHname) {
        try {
            ValidationResult validationResult = editUserValidator.validate(user);

            if (validationResult.isOK()) {
                userRepository.saveOrUpdateUser(user);
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
                if (registeredUser == null) {
                    registeredUser = new RegisteredUser();
                    subscription.setRegisteredUser(registeredUser);
                    registeredUser.setGitHubName(subscription.getGitHubName());
                    subscription.setRegisteredUser(registeredUser);
                }
                registeredUser.setWhitelisted(whitelist);
                userRepository.saveOrUpdateUser(registeredUser);
            }
            return subscriptions;
        } catch (HibernateException e) {
            throw new ApplicationException("Couldn't whitelist users.", e);
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

}
