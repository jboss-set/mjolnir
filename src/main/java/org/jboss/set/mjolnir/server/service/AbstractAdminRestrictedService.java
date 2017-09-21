package org.jboss.set.mjolnir.server.service;

import org.jboss.set.mjolnir.shared.domain.KerberosUser;

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
