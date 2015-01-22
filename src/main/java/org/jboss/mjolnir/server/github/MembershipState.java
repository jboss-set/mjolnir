package org.jboss.mjolnir.server.github;

/**
 * GitHub team membership states
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public final class MembershipState {

    private MembershipState() {
    }

    public static final String ACTIVE = "active";

    public static final String PENDING = "pending";

    public static final String NONE = "none";

}
