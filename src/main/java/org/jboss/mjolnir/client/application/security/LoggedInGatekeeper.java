package org.jboss.mjolnir.client.application.security;

import javax.inject.Inject;

import com.gwtplatform.mvp.client.annotations.DefaultGatekeeper;
import com.gwtplatform.mvp.client.proxy.Gatekeeper;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
@DefaultGatekeeper
public class LoggedInGatekeeper implements Gatekeeper {

    private CurrentUser2 currentUser;

    @Inject
    public LoggedInGatekeeper(CurrentUser2 currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    public boolean canReveal() {
        return currentUser.isLoggedIn();
    }
}
