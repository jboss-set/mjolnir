package org.jboss.set.mjolnir.server.bean;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.hibernate.HibernateException;
import org.jboss.logging.Logger;
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
            logger.debug("Retrieving team members");
            List<User> members = teamService.getMembers(teamId);
            logger.debugf("Retrieved %d users", members.size());
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
            logger.debug("Retrieving organization members");
            List<User> members = organizationService.getMembers(org);
            logger.debugf("Retrieved %d users", members.size());
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
        logger.debug("getOrganizationMembers");
        try {
            final List<SubscriptionSummary> subscriptionSummaries = new ArrayList<>();

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
                    //noinspection StatementWithEmptyBody
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
            final Map<String, Subscription> subscriptionMap = new HashMap<>(allUsers.size());
            //list needed to hold the null values, to avoid creating collection for each entry key
            final List<Subscription> result = new ArrayList<>();
            for (RegisteredUser user: allUsers) {
                final Subscription subscription = new Subscription();
                subscription.setRegisteredUser(user);
                subscription.setGitHubName(user.getGitHubName());
                subscription.setGitHubId(user.getGitHubId());

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
            for (Map.Entry<String, Subscription> entry : subscriptionMap.entrySet()) {
                if (checkedLdapUsers.get(entry.getKey())) {
                    entry.getValue().setActiveKerberosAccount(true);
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
                GithubTeam team = organizationRepository.getTeamByGithubId(teamId);
                if (team == null) {
                    throw new ApplicationException("Managing this team subscriptions not allowed: " + teamId);
                }

                if (entry.getValue()) {
                    try {
                        teamService.addMembership(teamId, gitHubName);
                    } catch (IOException e) {
                        logger.warn("Couldn't add membership", e);
                        throw new ApplicationException("Couldn't add membership: user: " + gitHubName + ", team: " + teamId, e);
                    }
                } else {
                    try {
                        teamService.removeMembership(teamId, gitHubName);
                    } catch (IOException e) {
                        logger.warn("Couldn't remove membership", e);
                        throw new ApplicationException("Couldn't remove membership: user: " + gitHubName + ", team: " + teamId, e);
                    }
                }
            }
    }

    public void unsubscribeUser(String orgName, String gitHubName) {
        try {
            GithubOrganization organization = organizationRepository.getOrganization(orgName);
            if (organization == null) {
                throw new ApplicationException("Managing this organization subscriptions not allowed: " + orgName);
            }

            organizationService.removeMember(orgName, gitHubName);
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }

    /**
     * Takes a list of GH users and returns a list of Subcription objects. Adds information about linked KRB name
     * and whether the KRB account is still active.
     */
    private List<Subscription> createSubscriptions(List<User> users) {
        logger.debugf("Transforming %d GH users to Subscription entities", users.size());

        // for each organization user create Subscription object
        final Map<Integer, Subscription> subscriptions = new HashMap<>();
        final Map<String, Subscription> ldapUsersToCheck = new HashMap<>();
        for (User user: users) {
            final String gitHubName = user.getLogin();

            final Subscription subscription = new Subscription();
            subscription.setGitHubId(user.getId());
            subscription.setGitHubName(gitHubName);
            subscriptions.put(user.getId(), subscription);
        }

        // find registered users by GH ID
        logger.debug("Looking for user registrations");
        final Map<Integer, RegisteredUser> registeredUsers = userRepository.getRegisteredUsersByGitHubIds(new ArrayList<>(subscriptions.keySet()));
        for (Map.Entry<Integer, RegisteredUser> entry: registeredUsers.entrySet()) {
            Integer githubId = entry.getKey();
            RegisteredUser registeredUser = entry.getValue();
            Subscription subscription = subscriptions.get(githubId);

            subscription.setRegisteredUser(registeredUser);
            if (StringUtils.isNotBlank(registeredUser.getGitHubName())) {
                // override GH username to what user provided during registration (this value is going to show up in
                // the members table as well as in the edit form)
                subscription.setGitHubName(registeredUser.getGitHubName());
            }
            ldapUsersToCheck.put(registeredUser.getKrbName(), subscription);
        }

        // check LDAP records for retrieved users
        logger.debugf("Looking for LDAP accounts of %d users", ldapUsersToCheck.size());
        final Map<String, Boolean> checkedLdapUsers = ldapRepository.checkUsersExists(ldapUsersToCheck.keySet());
        for (Map.Entry<String, Subscription> entry : ldapUsersToCheck.entrySet()) {
            if (checkedLdapUsers.get(entry.getKey())) {
                entry.getValue().setActiveKerberosAccount(true);
            }
        }

        return new ArrayList<>(subscriptions.values());
    }


    // setters

    public void setLdapRepository(LdapRepository ldapRepository) {
        this.ldapRepository = ldapRepository;
    }

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    public void setTeamService(ExtendedTeamService teamService) {
        this.teamService = teamService;
    }
}
