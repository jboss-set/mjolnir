package org.jboss.mjolnir.client.component;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import org.jboss.mjolnir.authentication.KerberosUser;
import org.jboss.mjolnir.client.CurrentUser;

/**
 * Displays user's GitHub name and link to change it.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class GitHubNamePanel extends Composite {

    public GitHubNamePanel() {
        final HTMLPanel gitHubNamePanel = new HTMLPanel("Your GitHub name is: <b>" + getGitHubName() + "</b> ");

        final Anchor modifyLink = new Anchor("Change it");
        modifyLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new ModifyGitHubNamePopup(true).show();
            }
        });
        gitHubNamePanel.add(modifyLink);

        initWidget(gitHubNamePanel);
    }

    private String getGitHubName() {
        final KerberosUser currentUser = CurrentUser.get();
        final String gitHubName;
        if (currentUser != null) {
            gitHubName = currentUser.getGithubName();
        } else {
            gitHubName = "undefined";
        }
        return gitHubName;
    }

}
