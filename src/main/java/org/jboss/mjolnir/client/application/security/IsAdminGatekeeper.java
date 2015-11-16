package org.jboss.mjolnir.client.application.security;

import javax.inject.Inject;

import com.gwtplatform.mvp.client.proxy.Gatekeeper;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class IsAdminGatekeeper implements Gatekeeper {

    private CurrentUser2 currentUser2;

    @Inject
    public IsAdminGatekeeper(CurrentUser2 currentUser2) {
        this.currentUser2 = currentUser2;
    }

    @Override
    public boolean canReveal() {
        return currentUser2.isLoggedIn() && currentUser2.getUser() != null
                && currentUser2.getUser().isAdmin();
    }
}
