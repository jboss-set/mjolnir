package org.jboss.mjolnir.client;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class NameTokens {
    public static final String MY_SUBSCRIPTIONS = "!subscriptions";
    public static final String LOGIN = "!login";
    public static final String ERROR = "!error";

    public static final String GITHUB_MEMBERS = "!github-members";
    public static final String REGISTERED_USERS = "!registered-users";

    public static String getOnLoginDefaultPage() {
        return MY_SUBSCRIPTIONS;
    }
}
