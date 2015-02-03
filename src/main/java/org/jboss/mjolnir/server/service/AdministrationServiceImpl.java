package org.jboss.mjolnir.server.service;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.jboss.mjolnir.authentication.GithubOrganization;
import org.jboss.mjolnir.authentication.KerberosUser;
import org.jboss.mjolnir.client.domain.Subscription;
import org.jboss.mjolnir.client.domain.SubscriptionSummary;
import org.jboss.mjolnir.client.exception.ApplicationException;
import org.jboss.mjolnir.client.service.AdministrationService;
import org.jboss.mjolnir.server.bean.ApplicationParameters;
import org.jboss.mjolnir.server.bean.GitHubRepository;
import org.jboss.mjolnir.server.bean.LdapRepository;
import org.jboss.mjolnir.server.bean.UserRepository;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    @Override
    public void init() throws ServletException {
        super.init();

        final String token = applicationParameters.getParameter(ApplicationParameters.GITHUB_TOKEN_KEY);

        final GitHubClient client = new GitHubClient();
        client.setOAuth2Token(token);
        organizationService = new OrganizationService(client);
    }

    /**
     * Provides list of all members of registered GitHub organizations together with information
     * whether they have an LDAP record.
     *
     * @return subscription summaries for registered GitHub organizations
     */
    public List<SubscriptionSummary> getSubscriptionsSummary() {
        try {
            final List<SubscriptionSummary> subscriptionSummaries = new ArrayList<SubscriptionSummary>();

            final Collection<GithubOrganization> organizations = gitHubRepository.getOrganizations();
            for (GithubOrganization organization : organizations) {
                final SubscriptionSummary summary = new SubscriptionSummary();
                summary.setOrganization(organization);

                final List<User> members = organizationService.getMembers(organization.getName());
                for (User member: members) {
                    final String gitHubName = member.getLogin();
                    final KerberosUser appUser = userRepository.getUserByGitHubName(gitHubName);
                    boolean existsInLdap = false;
                    if (appUser != null) {
                        existsInLdap = ldapRepository.checkUserRecord(appUser.getName());
                    }

                    final Subscription subscription = new Subscription();
                    subscription.setGitHubName(gitHubName);
                    subscription.setKerberosUser(appUser);
                    subscription.setActiveKerberosAccount(existsInLdap);
                    summary.getSubscriptions().add(subscription);
                }

                subscriptionSummaries.add(summary);
            }

            return subscriptionSummaries;
        } catch (Exception e) {
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
