package org.jboss.set.mjolnir.server.bean;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.jboss.set.mjolnir.shared.domain.GithubOrganization;
import org.jboss.set.mjolnir.shared.domain.RegisteredUser;
import org.jboss.set.mjolnir.shared.domain.Subscription;
import org.jboss.set.mjolnir.shared.domain.SubscriptionSummary;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class GitHubSubscriptionBeanTest {

    private static final String ORG_NAME = "testorg";
    private static final String KRB_USERNAME = "krb_user";
    private static final String GITHUB_USERNAME = "gh_user";

    private RegisteredUser appUser;
    private User gitHubUser;
    private GitHubSubscriptionBean gitHubSubscriptionBean;
    private OrganizationRepository organizationRepository;
    private OrganizationService organizationService;
    private UserRepository userRepository;
    private LdapRepository ldapRepository;

    @Before
    public void setup() throws SQLException, IOException {
        // prepare working data
        appUser = new RegisteredUser();
        appUser.setAdmin(true);
        appUser.setKrbName(KRB_USERNAME);

        gitHubUser = new User();
        gitHubUser.setLogin(GITHUB_USERNAME);

        // create mocked dependencies
        organizationRepository = Mockito.mock(OrganizationRepository.class);
        organizationService = Mockito.mock(OrganizationService.class);
        userRepository = Mockito.mock(UserRepository.class);
        ldapRepository = Mockito.mock(LdapRepository.class);

        // create service instance
        gitHubSubscriptionBean = new GitHubSubscriptionBean();
        gitHubSubscriptionBean.setOrganizationRepository(organizationRepository);
        gitHubSubscriptionBean.setOrganizationService(organizationService);
        gitHubSubscriptionBean.setUserRepository(userRepository);
        gitHubSubscriptionBean.setLdapRepository(ldapRepository);
    }

    @Test
    public void testGetSubscriptionSummaries() throws SQLException, IOException {
        // setup mocks
        Mockito.when(organizationRepository.getOrganizations()).thenReturn(Collections.singletonList(new GithubOrganization(ORG_NAME)));
        Mockito.when(organizationService.getMembers(ORG_NAME)).thenReturn(asList(gitHubUser));
        Mockito.when(userRepository.getUserByGitHubName(GITHUB_USERNAME)).thenReturn(appUser);
        Mockito.when(ldapRepository.checkUsersExists(Mockito.anySet())).thenReturn(Collections.singletonMap(KRB_USERNAME, true));

        // perform a call
        final List<SubscriptionSummary> subscriptionsSummary = gitHubSubscriptionBean.getOrganizationMembers();

        // checks
        Assert.assertEquals("Expected different number of organizations.", 1, subscriptionsSummary.size());
        Assert.assertEquals("Excepted different number of subscriptions.", 1, subscriptionsSummary.get(0).getSubscriptions().size());

        final Subscription subscription = subscriptionsSummary.get(0).getSubscriptions().get(0);

        Assert.assertEquals(GITHUB_USERNAME, subscription.getGitHubName());
        Assert.assertEquals(KRB_USERNAME, subscription.getKerberosName());
        Assert.assertEquals(true, subscription.isActiveKerberosAccount());
    }

    @Test
    public void getRegisteredUsersTest() throws SQLException {
        // setup mocks
        final List<RegisteredUser> registeredUsers = Arrays.asList(createUser("a", "a"), createUser("b", "b"));
        final Map<String, Boolean> usersInLdap = new HashMap<String, Boolean>();
        usersInLdap.put("a", true);
        usersInLdap.put("b", false);
        Mockito.when(userRepository.getAllUsers()).thenReturn(registeredUsers);
        Mockito.when(ldapRepository.checkUsersExists(Mockito.anySet())).thenReturn(usersInLdap);

        // perform a call
        final List<Subscription> result = gitHubSubscriptionBean.getRegisteredUsers();

        // checks
        Assert.assertEquals(2, result.size());
        Assert.assertEquals(true, findSubscriptionByKrbName(result, "a").isActiveKerberosAccount());
        Assert.assertEquals(false, findSubscriptionByKrbName(result, "b").isActiveKerberosAccount());
    }

    private RegisteredUser createUser(String krbName, String gitHubName) {
        final RegisteredUser appUser = new RegisteredUser();
        appUser.setKrbName(krbName);
        appUser.setGitHubName(gitHubName);
        return appUser;
    }

    private Subscription findSubscriptionByKrbName(List<Subscription> list, String krbName) {
        for (Subscription subscription: list) {
            if (krbName.equals(subscription.getKerberosName())) {
                return subscription;
            }
        }
        return null;
    }

}
