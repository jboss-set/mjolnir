package org.jboss.mjolnir.client;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Screen that will allow for a user to be able to add their github username upon logging in for the first time.
 *
 * @author: navssurtani
 * @version : 0.3
 */
public class RegisterGithubNameScreen extends AbstractGithubNameScreen {

    // We would need the kerberos-id of the user in order to add it on to the server side.
    public RegisterGithubNameScreen(String krb5Name) {
        super(krb5Name, "registerPanelContainer");
        buildAndDisplayForm();
    }

    private void buildAndDisplayForm() {
        // Here we do the work of displaying the form which will make an RPC call to edit a user's details.
        // Instantiate the handler. In this case, the isRegistered parameter would be false.
        NameHandler handler = new NameHandler(false);

        nameField = new TextBox();
        nameField.setTitle("Github username");
        nameField.addKeyUpHandler(handler);

        Button submitButton = buildSubmitButton();
        submitButton.addClickHandler(handler);

        formGrid = new Grid(3, 2);
        formGrid.setWidget(0, 0, new Label("Enter your github username"));
        formGrid.setWidget(0, 1, new Label("E.g.: 'myusername'."));
        formGrid.setWidget(1, 1, new Label("[This should NOT be the email address you use]"));
        formGrid.setWidget(2, 0, nameField);
        formGrid.setWidget(2, 1, submitButton);

        githubNamePanel.add(formGrid);
    }
}
