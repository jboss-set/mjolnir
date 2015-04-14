package org.jboss.mjolnir.server.service;

import org.jboss.mjolnir.authentication.KerberosUser;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class AbstractAdminRestrictedService extends AbstractServiceServlet {

    @Override
    protected boolean performAuthorization() {
        // user must be admin
        final KerberosUser user = getAuthenticatedUser();
        return user != null && user.isAdmin();
    }

}
