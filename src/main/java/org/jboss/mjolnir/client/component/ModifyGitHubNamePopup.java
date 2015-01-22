package org.jboss.mjolnir.client.component;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.client.rpc.XsrfTokenService;
import com.google.gwt.user.client.rpc.XsrfTokenServiceAsync;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SubmitButton;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.mjolnir.authentication.KerberosUser;
import org.jboss.mjolnir.client.CurrentUser;
import org.jboss.mjolnir.client.GitHubService;
import org.jboss.mjolnir.client.GitHubServiceAsync;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class ModifyGitHubNamePopup extends PopupPanel {

    private Logger logger = Logger.getLogger("");

    final GitHubServiceAsync gitHubService = GitHubService.Util.getInstance();

    public ModifyGitHubNamePopup(boolean allowCancel) {
        super(false);
        setGlassEnabled(true);

        final HTMLPanel panel = new HTMLPanel("");

        final TextBox textBox = new TextBox();
        textBox.setText(CurrentUser.get().getGithubName());
        panel.add(textBox);

        final SubmitButton submitButton = new SubmitButton("Save");
        panel.add(submitButton);

        if (allowCancel) {
            final Button cancelButton = new Button("Cancel");
            cancelButton.addClickHandler(new CancelClickHandler());
            panel.add(cancelButton);
        }

        final FormPanel form = new FormPanel();
        form.setWidget(panel);
        form.addSubmitHandler(new FormPanel.SubmitHandler() {
            @Override
            public void onSubmit(FormPanel.SubmitEvent event) {
                updateGitHubName(textBox.getText());
            }
        });

        setWidget(form);
    }

    private void updateGitHubName(final String newName) {
        final XsrfTokenServiceAsync xsrf = GWT.create(XsrfTokenService.class);
        ((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "xsrf");
        xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
            @Override
            public void onFailure(Throwable caught) {
                logger.log(Level.SEVERE, "Cant get XSRF token.", caught);
            }

            @Override
            public void onSuccess(XsrfToken result) {
                ((HasRpcToken) gitHubService).setRpcToken(result);
                gitHubService.modifyGithubName(newName, new AsyncCallback<KerberosUser>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        logger.log(Level.SEVERE, "Cant modify GitHub name.", caught);
                    }

                    @Override
                    public void onSuccess(KerberosUser result) {
                        CurrentUser.get().setGithubName(result.getGithubName());
                        ModifyGitHubNamePopup.this.hide();
                    }
                });
            }
        });
    }

    private class CancelClickHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            ModifyGitHubNamePopup.this.hide();
        }
    }

}
