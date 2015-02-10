package org.jboss.mjolnir.server.service;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.impl.StandardSerializationPolicy;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.jboss.mjolnir.authentication.GithubOrganization;
import org.jboss.mjolnir.authentication.KerberosUser;
import org.jboss.mjolnir.client.domain.Subscription;
import org.jboss.mjolnir.client.domain.SubscriptionSummary;
import org.jboss.mjolnir.client.exception.ApplicationException;
import org.jboss.mjolnir.server.bean.GitHubRepository;
import org.jboss.mjolnir.server.bean.LdapRepository;
import org.jboss.mjolnir.server.bean.UserRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class AdministrationServiceImplTest {

    private static final String ORG_NAME = "testorg";
    private static final String KRB_USERNAME = "krb_user";
    private static final String GITHUB_USERNAME = "gh_user";

    private KerberosUser appUser;
    private User gitHubUser;
    private AdministrationServiceImpl administrationService;
    private GitHubRepository gitHubRepository;
    private OrganizationService organizationService;
    private UserRepository userRepository;
    private LdapRepository ldapRepository;

    @Before
    public void setup() throws SQLException, IOException {
        // prepare working data
        appUser = new KerberosUser();
        appUser.setAdmin(true);
        appUser.setName(KRB_USERNAME);

        gitHubUser = new User();
        gitHubUser.setLogin(GITHUB_USERNAME);

        // create mocked dependencies
        gitHubRepository = Mockito.mock(GitHubRepository.class);
        organizationService = Mockito.mock(OrganizationService.class);
        userRepository = Mockito.mock(UserRepository.class);
        ldapRepository = Mockito.mock(LdapRepository.class);

        // create service instance
        administrationService = new AdministrationServiceImpl() {
            @Override
            protected KerberosUser getAuthenticatedUser() { // this is overridden to avoid getThreadLocalRequest() call
                return appUser;
            }
        };
        administrationService.setGitHubRepository(gitHubRepository);
        administrationService.setOrganizationService(organizationService);
        administrationService.setUserRepository(userRepository);
        administrationService.setLdapRepository(ldapRepository);
    }

    @Test
    public void testGetSubscriptionSummaries() throws SQLException, IOException {
        // setup mocks
        Mockito.when(gitHubRepository.getOrganizations()).thenReturn(Collections.singleton(new GithubOrganization(ORG_NAME)));
        Mockito.when(organizationService.getMembers(ORG_NAME)).thenReturn(asList(gitHubUser));
        Mockito.when(userRepository.getUserByGitHubName(GITHUB_USERNAME)).thenReturn(appUser);
        Mockito.when(ldapRepository.checkUsersExists(Mockito.anySet())).thenReturn(Collections.singletonMap(KRB_USERNAME, true));

        // perform a call
        final List<SubscriptionSummary> subscriptionsSummary = administrationService.getOrganizationMembers();

        // checks
        Assert.assertEquals("Expected different number of organizations.", 1, subscriptionsSummary.size());
        Assert.assertEquals("Excepted different number of subscriptions.", 1, subscriptionsSummary.get(0).getSubscriptions().size());

        final Subscription subscription = subscriptionsSummary.get(0).getSubscriptions().get(0);

        Assert.assertEquals(GITHUB_USERNAME, subscription.getGitHubName());
        Assert.assertEquals(KRB_USERNAME, subscription.getKerberosName());
        Assert.assertEquals(true, subscription.isActiveKerberosAccount());
    }

    @Test
    public void testUnauthorized() throws NoSuchMethodException, SerializationException {
        appUser.setAdmin(false);

        final String response = testAuthorization(appUser);
        Assert.assertTrue(response.startsWith("//EX"));
    }

    @Test
    public void testNotAuthenticated() throws NoSuchMethodException, SerializationException {
        final String response = testAuthorization(null);
        Assert.assertTrue(response.startsWith("//EX"));
    }

    @Test
    @Ignore // bypass CSRF protection
    public void testAuthorized() throws NoSuchMethodException, SerializationException {
        appUser.setAdmin(true);

        final String response = testAuthorization(appUser);
        Assert.assertTrue(response.startsWith("//OK"));
    }

    private String testAuthorization(final KerberosUser appUser) throws NoSuchMethodException, SerializationException {
        // overridden administration service that doesn't do anything is used in this test
        final AdministrationServiceImpl noOpAdministrationService = new AdministrationServiceImpl() {
            @Override
            public List<SubscriptionSummary> getOrganizationMembers() {
                return null; // just return null to bypass serialization
            }

            @Override
            protected KerberosUser getAuthenticatedUser() { // this is overridden to avoid getThreadLocalRequest() call
                return appUser;
            }
        };

        final Method method = AdministrationServiceImpl.class.getMethod("getOrganizationMembers", new Class[0]); // method to call
        final HashMap<Class<?>, Boolean> serializationWhitelist = new HashMap<Class<?>, Boolean>(); // classes to serialize
        serializationWhitelist.put(ApplicationException.class, true); // adding exception that is thrown when authorization fails
        final RPCRequest rpcRequest =
                new RPCRequest(method, new Object[0],
                        new StandardSerializationPolicy(serializationWhitelist, new HashMap<Class<?>, Boolean>(), new HashMap<Class<?>, String>()), 0); // request object
        return noOpAdministrationService.processCall(rpcRequest); // call processCall method, which is supposed to check authorization
    }
}
