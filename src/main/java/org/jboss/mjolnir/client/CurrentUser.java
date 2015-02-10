package org.jboss.mjolnir.client;

import org.jboss.mjolnir.authentication.KerberosUser;

/**
 * Singleton holding authenticated user (for client side usage).
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class CurrentUser {
    private static KerberosUser currentUser;

    private CurrentUser() {
    }

    public static KerberosUser get() {
        return currentUser;
    }

    public static void set(KerberosUser currentUser) {
        CurrentUser.currentUser = currentUser;
    }
}
