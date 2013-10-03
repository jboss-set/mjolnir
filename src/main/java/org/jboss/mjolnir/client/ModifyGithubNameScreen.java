package org.jboss.mjolnir.client;


import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.mjolnir.authentication.KerberosUser;

/**
 * Page that will contain a form which will allow users to modify their github username registered with this server
 * if the initial one is not correc.t.
 * This will not change their name on github itself.
 *
 * @version : 0.3
 * @author: navssurtani
 */
public class ModifyGithubNameScreen extends AbstractGithubNameScreen {


    public ModifyGithubNameScreen(String krb5Name) {
        super(krb5Name, "modifyPanelContainer");
        buildAndDisplayForm();
    }

    private void buildAndDisplayForm() {
        NameHandler handler = new NameHandler(true);

        nameField = new TextBox();
        nameField.setTitle("Github username");
        nameField.addKeyUpHandler(handler);

        Button submitButton = buildSubmitButton();
        submitButton.addClickHandler(handler);

        formGrid = new Grid(5, 4);
        formGrid.setWidget(0, 0, new Label("Hello there: "));
        formGrid.setWidget(0, 2, new Label(krb5Name));
        formGrid.setWidget(2, 0, new Label("Enter your correct github username."));
        formGrid.setWidget(2, 2, new Label("Note: It should NOT be an email address"));
        formGrid.setWidget(4, 0, nameField);
        formGrid.setWidget(4, 2, submitButton);

        githubNamePanel.add(formGrid);
    }
}
