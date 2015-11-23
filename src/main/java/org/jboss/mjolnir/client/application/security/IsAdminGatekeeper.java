package org.jboss.mjolnir.client.application.security;

import javax.inject.Inject;

import com.gwtplatform.mvp.client.proxy.Gatekeeper;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class IsAdminGatekeeper implements Gatekeeper {

    private CurrentUser currentUser;

    @Inject
    public IsAdminGatekeeper(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    public boolean canReveal() {
        return currentUser.isLoggedIn() && currentUser.getUser() != null
                && currentUser.getUser().isAdmin();
    }
}
