package org.jboss.mjolnir.client.application.security;

import org.jboss.mjolnir.shared.domain.KerberosUser;

/**
 * (Managed by GIN)
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class CurrentUser2 {

    private KerberosUser user;
    private boolean loggedIn;

    public KerberosUser getUser() {
        return user;
    }

    public void setUser(KerberosUser user) {
        this.user = user;
        if (user != null) {
            this.loggedIn = user.isLoggedIn();
        }
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void reset() {
        user = null;
        loggedIn = false;
    }
}
