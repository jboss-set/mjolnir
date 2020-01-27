package org.jboss.set.mjolnir.server.bean;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.hibernate.HibernateException;
import org.jboss.set.mjolnir.client.exception.ApplicationException;
import org.jboss.set.mjolnir.server.github.ExtendedTeamService;
import org.jboss.set.mjolnir.shared.domain.GithubOrganization;
import org.jboss.set.mjolnir.shared.domain.GithubTeam;
import org.jboss.set.mjolnir.shared.domain.RegisteredUser;
import org.jboss.set.mjolnir.shared.domain.Subscription;
import org.jboss.set.mjolnir.shared.domain.SubscriptionSummary;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
@Stateless
public class GitHubSubscriptionBean {

    private final Logger logger = Logger.getLogger(getClass().getName());

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
     * Retrieves users subscribed to a team.
     *
     * @param teamId GH team id
     * @return subscriptions
     */
    public List<Subscription> getTeamSubscriptions(int teamId) {
        try {
            List<User> members = teamService.getMembers(teamId);
            return createSubscriptions(members);
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }

    /**
     * Retrieves users subscribed to an organization.
     *
     * @param org GH organization
     * @return subscriptions
     */
    public List<Subscription> getOrganizationSubscriptions(String org) {
        try {
            List<User> members = organizationService.getMembers(org);
            return createSubscriptions(members);
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
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
            final List<GithubOrganization> organizations = organizationRepository.getOrganizations();
            for (GithubOrganization organization : organizations) {
                final SubscriptionSummary summary = new SubscriptionSummary();
                summary.setOrganization(organization);

                // for each organization member create Subscription object
                final List<User> members = organizationService.getMembers(organization.getName());

                List<Subscription> subscriptions = createSubscriptions(members);
                summary.getSubscriptions().addAll(subscriptions);
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
            final List<RegisteredUser> allUsers = userRepository.getAllUsers();

            // fetch users and create Subscription objects
            final Map<String, Subscription> subscriptionMap = new HashMap<String, Subscription>(allUsers.size());
            //list needed to hold the null values, to avoid creating collection for each entry key
            final List<Subscription> result = new ArrayList<Subscription>();
            for (RegisteredUser user: allUsers) {
                final Subscription subscription = new Subscription();
                subscription.setRegisteredUser(user);
                subscription.setGitHubName(user.getGithubName());

                if(user.getKrbName() != null) {
                    subscriptionMap.put(user.getKrbName(), subscription);
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
    public List<GithubOrganization> getSubscriptions(String gitHubName) {
        try {
            final List<GithubOrganization> organizations = organizationRepository.getOrganizations();
            for (GithubOrganization organization: organizations) {
                for (GithubTeam team: organization.getTeams()) {
                    final String membershipState = teamService.getMembership(team.getId(), gitHubName);
                    team.setMembershipState(membershipState);
                }
            }
            return organizations;
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }

    /**
     * Sets subscriptions of given GitHub user.
     *
     * @param gitHubName GitHub username
     * @param subscriptions subscription map (team id : subscribe?)
     */
    public void setSubscriptions(String gitHubName, Map<Integer, Boolean> subscriptions) {
            for (Map.Entry<Integer, Boolean> entry : subscriptions.entrySet()) {
                final Integer teamId = entry.getKey();
                if (entry.getValue()) {
                    try {
                        teamService.addMembership(teamId, gitHubName);
                    } catch (IOException e) {
                        logger.log(Level.WARNING, "Couldn't add membership", e);
                        throw new ApplicationException("Couldn't add membership: user: " + gitHubName + ", team: " + teamId, e);
                    }
                } else {
                    try {
                        teamService.removeMembership(teamId, gitHubName);
                    } catch (IOException e) {
                        logger.log(Level.WARNING, "Couldn't remove membership", e);
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

    /**
     * Takes a list of GH users and returns a list of Subcription objects. Adds information about linked KRB name
     * and whether the KRB account is still active.
     */
    private List<Subscription> createSubscriptions(List<User> users) {
        ArrayList<Subscription> subscriptions = new ArrayList<>();

        // for each organization user create Subscription object
        final Map<String, Subscription> ldapUsersToCheck = new HashMap<String, Subscription>();
        for (User user: users) {
            final String gitHubName = user.getLogin();

            final Subscription subscription = new Subscription();
            subscription.setGitHubName(gitHubName);
            subscriptions.add(subscription);

            final RegisteredUser appUser = userRepository.getUserByGitHubName(gitHubName);
            if (appUser != null) { // if user is registered, LDAP check will be done
                subscription.setRegisteredUser(appUser);
                ldapUsersToCheck.put(appUser.getKrbName(), subscription);
            }
        }

        // check LDAP records for retrieved users
        final Map<String, Boolean> checkedLdapUsers = ldapRepository.checkUsersExists(ldapUsersToCheck.keySet());
        for (Map.Entry<String, Boolean> checkedLdapUser: checkedLdapUsers.entrySet()) {
            if (checkedLdapUser.getValue()) {
                ldapUsersToCheck.get(checkedLdapUser.getKey()).setActiveKerberosAccount(true);
            }
        }

        return subscriptions;
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
