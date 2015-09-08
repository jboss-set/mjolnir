package org.jboss.mjolnir.server.bean;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.hibernate.HibernateException;
import org.jboss.mjolnir.authentication.GithubOrganization;
import org.jboss.mjolnir.authentication.GithubTeam;
import org.jboss.mjolnir.authentication.KerberosUser;
import org.jboss.mjolnir.client.domain.Subscription;
import org.jboss.mjolnir.client.domain.SubscriptionSummary;
import org.jboss.mjolnir.client.exception.ApplicationException;
import org.jboss.mjolnir.server.github.ExtendedTeamService;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
@Stateless
public class GitHubSubscriptionBean {

    @EJB
    private LdapRepository ldapRepository;

    @EJB
    private ApplicationParameters applicationParameters;

    @EJB
    private OrganizationRepository organizationRepository;

    @EJB
    private UserRepository userRepository;

    private OrganizationService organizationService;
    private ExtendedTeamService teamService;


    @PostConstruct
    public void initBean() {
        final String token = applicationParameters.getMandatoryParameter(ApplicationParameters.GITHUB_TOKEN_KEY);

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
            final Set<GithubOrganization> organizations = organizationRepository.getOrganizations();
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

    public void removeFromOrganizations(String gitHubName) {
        try {
            for (GithubOrganization org : organizationRepository.getOrganizations()) {
                try {
                    organizationService.removeMember(org.getName(), gitHubName);
                } catch (RequestException e) {
                    if (e.getStatus() == 404) {
                        // that's fine => user was not subscribed anyway
                    } else {
                        throw e;
                    }
                }
            }
        } catch (Exception e) {
            throw new ApplicationException(e);
        }
    }

    /**
     * Retrieves registered users and their LDAP status (exists or not).
     *
     * @return subscription objects representing registered users
     */
    public List<Subscription> getRegisteredUsers() {
        try {
            final List<KerberosUser> allUsers = userRepository.getAllUsers();

            // fetch users and create Subscription objects
            final Map<String, Subscription> subscriptionMap = new HashMap<String, Subscription>(allUsers.size());
            //list needed to hold the null values, to avoid creating collection for each entry key
            final List<Subscription> result = new ArrayList<Subscription>();
            for (KerberosUser user: allUsers) {
                final Subscription subscription = new Subscription();
                subscription.setKerberosUser(user);
                subscription.setGitHubName(user.getGithubName());

                if(user.getName() != null) {
                    subscriptionMap.put(user.getName(), subscription);
                } else {
                    //cannot be active when the value is null
                    subscription.setActiveKerberosAccount(false);
                    result.add(subscription);
                }
            }

            // check LDAP records for retrieved users
            final Map<String, Boolean> checkedLdapUsers = ldapRepository.checkUsersExists(subscriptionMap.keySet());
            for (Map.Entry<String, Boolean> checkedLdapUser : checkedLdapUsers.entrySet()) {
                if (checkedLdapUser.getValue()) {
                    subscriptionMap.get(checkedLdapUser.getKey()).setActiveKerberosAccount(true);
                }
            }

            result.addAll(0, subscriptionMap.values());
            return result;
        } catch (HibernateException e) {
            throw new ApplicationException(e);
        }
    }

    /**
     * Retrieves subscriptions of given GitHub user.
     *
     * @param gitHubName GitHub username
     * @return subscription data
     */
    public Set<GithubOrganization> getSubscriptions(String gitHubName) {
        try {
            final Set<GithubOrganization> organizations = organizationRepository.getOrganizations();
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

    /**
     * Sets subscriptions of given GitHub user.
     *
     * @param gitHubName GitHub username
     * @param subscriptions subscription map (team id => subscribe?)
     */
    public void setSubscriptions(String gitHubName, Map<Integer, Boolean> subscriptions) {
            for (Map.Entry<Integer, Boolean> entry : subscriptions.entrySet()) {
                final Integer teamId = entry.getKey();
                if (entry.getValue()) {
                    try {
                        teamService.addMembership(teamId, gitHubName);
                    } catch (IOException e) {
                        throw new ApplicationException("Couldn't add membership: user: " + gitHubName + ", team: " + teamId, e);
                    }
                } else {
                    try {
                        teamService.removeMembership(teamId, gitHubName);
                    } catch (IOException e) {
                        throw new ApplicationException("Couldn't remove membership: user: " + gitHubName + ", team: " + teamId, e);
                    }
                }
            }
    }

    public void unsubscribeUser(String organization, String gitHubName) {
        try {
            organizationService.removeMember(organization, gitHubName);
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }


    // setters

    public void setLdapRepository(LdapRepository ldapRepository) {
        this.ldapRepository = ldapRepository;
    }

    public void setApplicationParameters(ApplicationParameters applicationParameters) {
        this.applicationParameters = applicationParameters;
    }

    public void setOrganizationRepository(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void setOrganizationService(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    public void setTeamService(ExtendedTeamService teamService) {
        this.teamService = teamService;
    }
}
