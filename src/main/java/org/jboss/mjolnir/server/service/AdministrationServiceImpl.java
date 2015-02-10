package org.jboss.mjolnir.server.service;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.jboss.mjolnir.authentication.GithubOrganization;
import org.jboss.mjolnir.authentication.GithubTeam;
import org.jboss.mjolnir.authentication.KerberosUser;
import org.jboss.mjolnir.client.domain.Subscription;
import org.jboss.mjolnir.client.domain.SubscriptionSummary;
import org.jboss.mjolnir.client.exception.ApplicationException;
import org.jboss.mjolnir.client.service.AdministrationService;
import org.jboss.mjolnir.server.bean.ApplicationParameters;
import org.jboss.mjolnir.server.bean.GitHubRepository;
import org.jboss.mjolnir.server.bean.LdapRepository;
import org.jboss.mjolnir.server.bean.UserRepository;
import org.jboss.mjolnir.server.github.ExtendedTeamService;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Performs administration tasks.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class AdministrationServiceImpl extends AbstractServiceServlet implements AdministrationService {

    @EJB
    private LdapRepository ldapRepository;

    @EJB
    private ApplicationParameters applicationParameters;

    @EJB
    private GitHubRepository gitHubRepository;

    @EJB
    private UserRepository userRepository;

    private OrganizationService organizationService;
    private ExtendedTeamService teamService;

    @Override
    public void init() throws ServletException {
        super.init();

        final String token = applicationParameters.getParameter(ApplicationParameters.GITHUB_TOKEN_KEY);

        final GitHubClient client = new GitHubClient();
        client.setOAuth2Token(token);
        organizationService = new OrganizationService(client);
        teamService = new ExtendedTeamService(client);
    }

    /**
     * Provides list of all members of registered GitHub organizations together with information
     * whether they have an LDAP record.
     *
     * @return subscription summaries for registered GitHub organizations
     */
    public List<SubscriptionSummary> getOrganizationMembers() {
        try {
            final List<SubscriptionSummary> subscriptionSummaries = new ArrayList<SubscriptionSummary>();

            // create single SubscriptionSummary for each organization
            final Set<GithubOrganization> organizations = gitHubRepository.getOrganizations();
            for (GithubOrganization organization : organizations) {
                final SubscriptionSummary summary = new SubscriptionSummary();
                summary.setOrganization(organization);

                // for each organization member create Subscription object
                final List<User> members = organizationService.getMembers(organization.getName());
                final Map<String, Subscription> ldapUsersToCheck = new HashMap<String, Subscription>();
                for (User member: members) {
                    final String gitHubName = member.getLogin();

                    final Subscription subscription = new Subscription();
                    subscription.setGitHubName(gitHubName);
                    summary.getSubscriptions().add(subscription);

                    final KerberosUser appUser = userRepository.getUserByGitHubName(gitHubName);
                    if (appUser != null) { // if user is registered, LDAP check will be done
                        subscription.setKerberosUser(appUser);
                        ldapUsersToCheck.put(appUser.getName(), subscription);
                    }
                }

                // check LDAP records for retrieved users
                final Map<String, Boolean> checkedLdapUsers = ldapRepository.checkUsersExists(ldapUsersToCheck.keySet());
                for (Map.Entry<String, Boolean> checkedLdapUser: checkedLdapUsers.entrySet()) {
                    if (checkedLdapUser.getValue()) {
                        ldapUsersToCheck.get(checkedLdapUser.getKey()).setActiveKerberosAccount(true);
                    }
                }

                subscriptionSummaries.add(summary);
            }

            return subscriptionSummaries;
        } catch (Exception e) {
            throw new ApplicationException(e);
        }
    }

    @Override
    public List<Subscription> getRegisteredUsers() {
        try {
            final List<KerberosUser> allUsers = userRepository.getAllUsers();

            // fetch users and create Subscription objects
            final Map<String, Subscription> subscriptionMap = new HashMap<String, Subscription>(allUsers.size());
            for (KerberosUser user: allUsers) {
                final Subscription subscription = new Subscription();
                subscription.setKerberosUser(user);
                subscription.setGitHubName(user.getGithubName());
                subscriptionMap.put(user.getName(), subscription);
            }

            // check LDAP records for retrieved users
            final Map<String, Boolean> checkedLdapUsers = ldapRepository.checkUsersExists(subscriptionMap.keySet());
            for (Map.Entry<String, Boolean> checkedLdapUser: checkedLdapUsers.entrySet()) {
                if (checkedLdapUser.getValue()) {
                    subscriptionMap.get(checkedLdapUser.getKey()).setActiveKerberosAccount(true);
                }
            }

            return new ArrayList<Subscription>(subscriptionMap.values());
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
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
    public void editUser(KerberosUser user) {
        try {
            userRepository.saveUser(user);
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }

    @Override
    public Set<GithubOrganization> getSubscriptions(String gitHubName) {
        try {
            final Set<GithubOrganization> organizations = gitHubRepository.getOrganizations();
            for (GithubOrganization organization: organizations) {
                for (GithubTeam team: organization.getTeams()) {
                    final String membershipState = teamService.getMembership(team.getId(), gitHubName);
                    team.setMembershipState(membershipState);
                }
            }
            return organizations;
        } catch (SQLException e) {
            throw new ApplicationException(e);
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }

    @Override
    public void setSubscriptions(String gitHubName, Map<Integer, Boolean> subscriptions) {
        try {
            for (Map.Entry<Integer, Boolean> entry : subscriptions.entrySet()) {
                final Integer teamId = entry.getKey();
                if (entry.getValue()) {
                    teamService.addMembership(teamId, gitHubName);
                } else {
                    teamService.removeMembership(teamId, gitHubName);
                }
            }
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }

    @Override
    public void removeFromAllOrganizations(String gitHubName) {
        try {
            final Set<GithubOrganization> organizations = gitHubRepository.getOrganizations();
            for (GithubOrganization organization: organizations) {
                organizationService.removeMember(organization.getName(), gitHubName);
            }
        } catch (IOException e) {
            throw new ApplicationException(e);
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }

    public void setLdapRepository(LdapRepository ldapRepository) {
        this.ldapRepository = ldapRepository;
    }

    public void setApplicationParameters(ApplicationParameters applicationParameters) {
        this.applicationParameters = applicationParameters;
    }

    public void setGitHubRepository(GitHubRepository gitHubRepository) {
        this.gitHubRepository = gitHubRepository;
    }

    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void setOrganizationService(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @Override
    protected boolean performAuthorization() {
        // user must be admin
        final KerberosUser user = getAuthenticatedUser();
        return user != null && user.isAdmin();
    }
}
