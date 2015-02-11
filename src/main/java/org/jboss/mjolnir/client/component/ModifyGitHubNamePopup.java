package org.jboss.mjolnir.client.component;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.client.rpc.XsrfTokenService;
import com.google.gwt.user.client.rpc.XsrfTokenServiceAsync;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.mjolnir.authentication.KerberosUser;
import org.jboss.mjolnir.client.CurrentUser;
import org.jboss.mjolnir.client.ExceptionHandler;
import org.jboss.mjolnir.client.exception.GitHubNameAlreadyTakenException;
import org.jboss.mjolnir.client.service.GitHubService;
import org.jboss.mjolnir.client.service.GitHubServiceAsync;

import java.util.logging.Logger;

/**
 * Popup allowing user to change his GitHub name
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class ModifyGitHubNamePopup extends PopupPanel {

    interface Binder extends UiBinder<Widget, ModifyGitHubNamePopup> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    private Logger logger = Logger.getLogger("");
    private GitHubServiceAsync gitHubService = GitHubService.Util.getInstance();

    @UiField
    Label messageLabel;

    @UiField
    TextBox textBox;

    @UiField
    Button submitButton;

    @UiField
    Button cancelButton;

    @UiField
    Label feedbackLabel;

    /**
     * @param allowCancel can user close the popup without submitting?
     */
    public ModifyGitHubNamePopup(boolean allowCancel) {
        this(allowCancel, null);
    }

    /**
     * @param allowCancel can user close the popup without submitting?
     * @param message     dialog message to display
     */
    public ModifyGitHubNamePopup(boolean allowCancel, String message) {
        super(false); // no auto hide
        setGlassEnabled(true); // forbid to click outside the popup
        setWidget(uiBinder.createAndBindUi(this));

        if (message != null) {
            messageLabel.setText(message);
        }

        textBox.setText(CurrentUser.get().getGithubName());

        submitButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                updateGitHubName(textBox.getText());
            }
        });

        cancelButton.addClickHandler(new CancelClickHandler());
        cancelButton.setEnabled(allowCancel);
    }

    private void updateGitHubName(final String newName) {
        final XsrfTokenServiceAsync xsrf = GWT.create(XsrfTokenService.class);
        ((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "xsrf");
        xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
            @Override
            public void onFailure(Throwable caught) {
                ExceptionHandler.handle("Cant get XSRF token.", caught);
            }

            @Override
            public void onSuccess(XsrfToken result) {
                ((HasRpcToken) gitHubService).setRpcToken(result);
                gitHubService.modifyGithubName(newName, new AsyncCallback<KerberosUser>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        if (caught instanceof GitHubNameAlreadyTakenException) {
                            feedbackLabel.setText("Error: " + caught.getMessage());
                            return;
                        }
                        ExceptionHandler.handle("Cant modify GitHub name.", caught);
                    }

                    @Override
                    public void onSuccess(KerberosUser updatedUser) {
                        CurrentUser.get().setGithubName(updatedUser.getGithubName());
                        ModifyGitHubNamePopup.this.hide();
                        onSaved(updatedUser);
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

    /**
     * Callback method called after new GitHub name was saved.
     *
     * @param modifiedUser user with modified values
     */
    protected void onSaved(KerberosUser modifiedUser) {
    }

}
