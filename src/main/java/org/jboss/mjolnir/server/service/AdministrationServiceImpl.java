package org.jboss.mjolnir.server.service;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.UserService;
import org.hibernate.HibernateException;
import org.jboss.mjolnir.authentication.GithubOrganization;
import org.jboss.mjolnir.authentication.KerberosUser;
import org.jboss.mjolnir.client.domain.EntityUpdateResult;
import org.jboss.mjolnir.client.domain.Subscription;
import org.jboss.mjolnir.client.domain.SubscriptionSummary;
import org.jboss.mjolnir.client.domain.ValidationResult;
import org.jboss.mjolnir.client.exception.ApplicationException;
import org.jboss.mjolnir.client.service.AdministrationService;
import org.jboss.mjolnir.server.bean.ApplicationParameters;
import org.jboss.mjolnir.server.bean.GitHubSubscriptionBean;
import org.jboss.mjolnir.server.bean.LdapRepository;
import org.jboss.mjolnir.server.bean.UserRepository;
import org.jboss.mjolnir.server.service.validation.*;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private Validator<KerberosUser> validator;
    private Validation<KerberosUser> krbNameValidation;
    private Validation<KerberosUser> githubNameValidation;

    @Override
    public void init() throws ServletException {
        super.init();

        String token = applicationParameters.getMandatoryParameter(ApplicationParameters.GITHUB_TOKEN_KEY);

        GitHubClient client = new GitHubClient();
        client.setOAuth2Token(token);
        UserService userService = new UserService(client);

        validator = new Validator<KerberosUser>();

        krbNameValidation = new KrbNameTakenValidation(userRepository);
        githubNameValidation = new GitHubNameTakenValidation(userRepository);
        validator.addValidation(krbNameValidation);
        validator.addValidation(githubNameValidation);
        validator.addValidation(new GitHubNameExistsValidation(userService));
    }

    /**
     * @see org.jboss.mjolnir.server.bean.GitHubSubscriptionBean#getOrganizationMembers()
     */
    public List<SubscriptionSummary> getOrganizationMembers() {
        return gitHubSubscriptionBean.getOrganizationMembers();
    }

    /**
     * @see org.jboss.mjolnir.server.bean.GitHubSubscriptionBean#getRegisteredUsers()
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
    public EntityUpdateResult<KerberosUser> registerUser(KerberosUser user) {
        try {
            ValidationResult validationResult = validator.validate(user);

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
    public void deleteUser(KerberosUser user) {
        try {
            userRepository.deleteUser(user);
        } catch (HibernateException e) {
            throw new ApplicationException(e);
        }
    }

    @Override
    public void deleteUsers(Collection<KerberosUser> users) {
        try {
            userRepository.deleteUsers(users);
        } catch (HibernateException e) {
            throw new ApplicationException(e);
        }
    }

    @Override
    public EntityUpdateResult<KerberosUser> editUser(KerberosUser user, boolean validateKrbName, boolean validateGHname) {
        try {
            if(!validateKrbName) {
                validator.removeValidation(krbNameValidation);
            }
            if(!validateGHname) {
                validator.removeValidation(githubNameValidation);
            }
            ValidationResult validationResult = validator.validate(user);
            validator.addValidation(krbNameValidation);
            validator.addValidation(githubNameValidation);


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
     * @see org.jboss.mjolnir.server.bean.GitHubSubscriptionBean#getSubscriptions(String)
     */
    @Override
    public Set<GithubOrganization> getSubscriptions(String gitHubName) {
        return gitHubSubscriptionBean.getSubscriptions(gitHubName);
    }

    @Override
    public void unsubscribe(Collection<Subscription> subscriptions) throws ApplicationException {
        for (Subscription subscription: subscriptions) {
            gitHubSubscriptionBean.removeFromOrganizations(subscription.getGitHubName());
        }
    }

    /**
     * @see org.jboss.mjolnir.server.bean.GitHubSubscriptionBean#setSubscriptions(String, java.util.Map)
     */
    @Override
    public void setSubscriptions(String gitHubName, Map<Integer, Boolean> subscriptions) {
        gitHubSubscriptionBean.setSubscriptions(gitHubName, subscriptions);
    }

    @Override
    public Collection<Subscription> whitelist(Collection<Subscription> subscriptions, boolean whitelist) {
        try {
            for (Subscription subscription : subscriptions) {
                KerberosUser kerberosUser = subscription.getKerberosUser();
                if (kerberosUser == null) {
                    kerberosUser = new KerberosUser();
                    subscription.setKerberosUser(kerberosUser);
                    kerberosUser.setGithubName(subscription.getGitHubName());
                    subscription.setKerberosUser(kerberosUser);
                }
                kerberosUser.setWhitelisted(whitelist);
                userRepository.saveOrUpdateUser(kerberosUser);
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
