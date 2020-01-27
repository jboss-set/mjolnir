package org.jboss.set.mjolnir.client.application.security;

import org.jboss.set.mjolnir.shared.domain.RegisteredUser;

/**
 * (Managed by GIN)
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class CurrentUser {

    private RegisteredUser user;

    public RegisteredUser getUser() {
        return user;
    }

    public void setUser(RegisteredUser user) {
        this.user = user;
    }

    public boolean isLoggedIn() {
        return user != null && user.isLoggedIn();
    }

    public void reset() {
        user = new RegisteredUser();
    }
}
