package org.jboss.mjolnir.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.jboss.mjolnir.authentication.KerberosUser;

/**
 * Class that will essentially have the place-holders for the common UI's in the Add and Modify name screens.
 *
 * @version : 0.3
 * @author: navssurtani
 */
public abstract class AbstractGithubNameScreen extends Composite {
    protected String krb5Name;
    protected RootPanel githubNamePanel;
    protected LoginServiceAsync loginService;
    protected TextBox nameField;
    protected Grid formGrid;

    protected AbstractGithubNameScreen(String krb5Name, String panelName) {
        this.krb5Name = krb5Name;
        this.githubNamePanel = RootPanel.get(panelName);
        this.loginService = LoginService.Util.getInstance();
    }

    protected Button buildSubmitButton() {
        Button b = new Button("Submit");
        b.setEnabled(true);
        b.getElement().setId("Submit");
        return b;
    }

    protected void displayPopupBox(String header, String message) {
        final DialogBox box = new DialogBox();
        box.setText(header);
        final HTML html = new HTML();
        html.setHTML(message);
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        final Button closeButton = buildCloseButton(box);
        verticalPanel.add(html);
        verticalPanel.add(closeButton);
        box.setWidget(verticalPanel);
        box.center();
    }

    private Button buildCloseButton(final DialogBox box) {
        final Button closeButton = new Button("Close");
        closeButton.setEnabled(true);
        closeButton.getElement().setId("Close");
        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                box.hide();
            }
        });
        return closeButton;
    }

    private void executeRegistration() {
        loginService.registerKerberosUser(krb5Name, nameField.getText(), new AsyncCallback<KerberosUser>() {
            @Override
            public void onFailure(Throwable caught) {
                displayPopupBox("Error with registration", caught.getMessage());
            }

            @Override
            public void onSuccess(KerberosUser user) {
                githubNamePanel.remove(formGrid);
                EntryPage.getInstance().moveToSubscriptionScreen(user.getName());
            }
        });
    }

    private void executeUpdate() {
        // This method is not supported as yet.
        displayPopupBox("Unsupported!", "You should not see this error box!");
    }

    protected class NameHandler implements ClickHandler, KeyUpHandler {

        private final boolean isRegistered;

        protected NameHandler(boolean isRegistered) {
            this.isRegistered = isRegistered;
        }

        @Override
        public void onClick(ClickEvent event) {
            if (!isRegistered) executeRegistration();
            else executeUpdate();
        }

        @Override
        public void onKeyUp(KeyUpEvent event) {
            if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                if (!isRegistered) executeRegistration();
                else executeUpdate();
            }
        }
    }
}
