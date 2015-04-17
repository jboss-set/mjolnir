package org.jboss.mjolnir.server.service;

import org.jboss.mjolnir.authentication.GithubOrganization;
import org.jboss.mjolnir.authentication.KerberosUser;
import org.jboss.mjolnir.client.domain.Subscription;
import org.jboss.mjolnir.client.domain.SubscriptionSummary;
import org.jboss.mjolnir.client.exception.ApplicationException;
import org.jboss.mjolnir.client.exception.GitHubNameAlreadyTakenException;
import org.jboss.mjolnir.client.service.AdministrationService;
import org.jboss.mjolnir.server.bean.ApplicationParameters;
import org.jboss.mjolnir.server.bean.GitHubSubscriptionBean;
import org.jboss.mjolnir.server.bean.UserRepository;

import javax.ejb.EJB;
import java.sql.SQLException;
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
    public void deleteUser(KerberosUser user) {
        try {
            userRepository.deleteUser(user.getName());
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }

    @Override
    public void deleteUsers(Collection<KerberosUser> users) {
        try {
            userRepository.deleteUsers(users);
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }

    @Override
    public void editUser(KerberosUser user) throws GitHubNameAlreadyTakenException {
        try {
            final KerberosUser userByGitHubName = userRepository.getUserByGitHubName(user.getGithubName());
            if (userByGitHubName != null && !userByGitHubName.equals(user)) {
                throw new GitHubNameAlreadyTakenException("This GitHub name is already taken by different user.");
            }

            userRepository.saveUser(user);
        } catch (SQLException e) {
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
                    kerberosUser.setGithubName(subscription.getGitHubName());
                    subscription.setKerberosUser(kerberosUser);
                }
                kerberosUser.setWhitelisted(whitelist);
                userRepository.saveUser(kerberosUser);
            }
            return subscriptions;
        } catch (SQLException e) {
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
