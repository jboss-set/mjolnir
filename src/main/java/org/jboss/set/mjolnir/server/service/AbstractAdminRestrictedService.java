package org.jboss.set.mjolnir.server.service;

import org.jboss.set.mjolnir.shared.domain.RegisteredUser;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class AbstractAdminRestrictedService extends AbstractServiceServlet {

    @Override
    protected boolean performAuthorization() {
        // user must be admin
        final RegisteredUser user = getAuthenticatedUser();
        return user != null && user.isAdmin();
    }

}
