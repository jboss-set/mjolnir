package org.jboss.mjolnir.server.service;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.impl.StandardSerializationPolicy;
import org.jboss.mjolnir.authentication.KerberosUser;
import org.jboss.mjolnir.client.domain.SubscriptionSummary;
import org.jboss.mjolnir.client.exception.ApplicationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class AdministrationServiceImplTest {

    private static final String KRB_USERNAME = "krb_user";

    private KerberosUser appUser;

    @Before
    public void setup() throws SQLException, IOException {
        // prepare working data
        appUser = new KerberosUser();
        appUser.setAdmin(true);
        appUser.setName(KRB_USERNAME);
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
