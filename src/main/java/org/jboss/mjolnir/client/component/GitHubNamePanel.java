package org.jboss.mjolnir.client.component;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.mjolnir.authentication.KerberosUser;
import org.jboss.mjolnir.client.CurrentUser;

/**
 * Displays user's GitHub name and link to change it.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class GitHubNamePanel extends Composite {

    interface Binder extends UiBinder<Widget, GitHubNamePanel> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    @UiField
    Label gitHubNameLabel;

    @UiField
    Button changeButton;

    public GitHubNamePanel(final SubscriptionScreen screen) {
        initWidget(uiBinder.createAndBindUi(this));

        gitHubNameLabel.setText(CurrentUser.get().getGithubName());

        changeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final ModifyGitHubNamePopup popup = new ModifyGitHubNamePopup(true) {
                    @Override
                    protected void onSaved(KerberosUser modifiedUser) {
                        // update displayed label value
                        gitHubNameLabel.setText(modifiedUser.getGithubName());
                        screen.reloadSubscriptions();
                    }
                };
                popup.center();
            }
        });
    }
}
