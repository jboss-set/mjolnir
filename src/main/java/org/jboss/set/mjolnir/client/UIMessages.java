package org.jboss.set.mjolnir.client;

import com.google.gwt.i18n.client.Messages;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public interface UIMessages extends Messages {

    @DefaultMessage("https://www.github.com/{0}")
    String organizationUrl(String organizationName);

    @DefaultMessage("Invitation Sent")
    String invitationSentCaption();

    @DefaultMessage("<p><b>Important:</b> to complete the process, please continue to</p>" +
            "<p><a href=\"{0}\" target=\"_blank\">{0}</a><p/>" +
            "<p>and follow the invitation link on the page. After accepting the invitation, you will " +
            "be able to access private repositories.</p>" +
            "<p>(This is not necessary if you already were a member of this GitHub organization.)</p>")
    String invitationSentMessage(String organizationUrl);

    @DefaultMessage("Yes")
    String yes();

    @DefaultMessage("No")
    String no();

    @DefaultMessage("pending (invitation sent)")
    String subscribtionStatusPendnig();

    @DefaultMessage("Subscribe")
    String subscribe();

    @DefaultMessage("Unsubscribe")
    String unsubscribe();

    @DefaultMessage("Subscribtion")
    String subscribtion();

    @DefaultMessage("Membership")
    String membership();

    @DefaultMessage("Team")
    String team();

}
