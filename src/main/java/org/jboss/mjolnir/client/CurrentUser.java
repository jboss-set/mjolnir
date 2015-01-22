package org.jboss.mjolnir.client;

import org.jboss.mjolnir.authentication.KerberosUser;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class CurrentUser {
    private static KerberosUser currentUser;

    public static KerberosUser get() {
        return currentUser;
    }

    public static void set(KerberosUser currentUser) {
        CurrentUser.currentUser = currentUser;
    }
}
