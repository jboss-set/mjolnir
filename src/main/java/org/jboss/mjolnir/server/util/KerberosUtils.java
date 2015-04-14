package org.jboss.mjolnir.server.util;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class KerberosUtils {

    /**
     * Strips username of "@redhat.com" part.
     *
     * @param username
     * @return
     */
    public static String normalizeUsername(String username) {
        String normalizedName = username;
        if (username.contains("@")) {
            normalizedName = username.substring(0, username.indexOf("@"));
        }
        return normalizedName;
    }

}
