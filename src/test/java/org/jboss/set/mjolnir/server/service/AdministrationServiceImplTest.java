package org.jboss.set.mjolnir.server.service;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.impl.StandardSerializationPolicy;
import org.jboss.set.mjolnir.shared.domain.RegisteredUser;
import org.jboss.set.mjolnir.shared.domain.SubscriptionSummary;
import org.jboss.set.mjolnir.client.exception.ApplicationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class AdministrationServiceImplTest {

    private static final String KRB_USERNAME = "krb_user";

    private RegisteredUser appUser;

    @Before
    public void setup() {
        // prepare working data
        appUser = new RegisteredUser();
        appUser.setAdmin(true);
        appUser.setKrbName(KRB_USERNAME);
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

    private String testAuthorization(final RegisteredUser appUser) throws NoSuchMethodException, SerializationException {
        // overridden administration service that doesn't do anything is used in this test
        final AdministrationServiceImpl noOpAdministrationService = new AdministrationServiceImpl() {
            @Override
            public List<SubscriptionSummary> getOrganizationMembers() {
                return null; // just return null to bypass serialization
            }

            @Override
            protected RegisteredUser getAuthenticatedUser() { // this is overridden to avoid getThreadLocalRequest() call
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
