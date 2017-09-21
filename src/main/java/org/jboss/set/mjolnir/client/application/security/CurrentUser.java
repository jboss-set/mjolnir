package org.jboss.set.mjolnir.client.application.security;

import org.jboss.set.mjolnir.shared.domain.KerberosUser;

/**
 * (Managed by GIN)
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class CurrentUser {

    private KerberosUser user;

    public KerberosUser getUser() {
        return user;
    }

    public void setUser(KerberosUser user) {
        this.user = user;
    }

    public boolean isLoggedIn() {
        return user != null && user.isLoggedIn();
    }

    public void reset() {
        user = new KerberosUser();
    }
}
