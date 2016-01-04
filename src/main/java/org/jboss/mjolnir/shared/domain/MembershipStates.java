package org.jboss.mjolnir.shared.domain;

/**
 * GitHub team membership states
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public final class MembershipStates {

    private MembershipStates() {
    }

    public static final String ACTIVE = "active";

    public static final String PENDING = "pending";

    public static final String NONE = "none";

}
