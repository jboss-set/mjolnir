package org.jboss.mjolnir.client.component.administration;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.mjolnir.authentication.KerberosUser;
import org.jboss.mjolnir.client.ExceptionHandler;
import org.jboss.mjolnir.client.service.AdministrationService;
import org.jboss.mjolnir.client.service.AdministrationServiceAsync;

/**
 * Dialog for modifying users.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class EditUserDialog extends DialogBox {

    interface Binder extends UiBinder<Widget, EditUserDialog> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    private AdministrationServiceAsync administrationService = AdministrationService.Util.getInstance();

    @UiField
    TextBox kerberosNameBox;

    @UiField
    TextBox gitHubNameBox;

    @UiField
    Button submitButton;

    @UiField
    Button cancelButton;

    public EditUserDialog(final KerberosUser user) {
        setGlassEnabled(true);
        setWidget(uiBinder.createAndBindUi(this));
        setText("Edit User");

        if (user != null && user.getName() != null) { // username is not modifiable for existing users
            kerberosNameBox.setEnabled(false);
        }

        // set field values
        if (user != null) {
            kerberosNameBox.setText(user.getName());
            gitHubNameBox.setText(user.getGithubName());
        }

        submitButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final KerberosUser userToSave = new KerberosUser();
                if (user == null || user.getName() == null) { // username is not modifiable for existing users
                    userToSave.setName(kerberosNameBox.getText());
                } else {
                    userToSave.setName(user.getName());
                }
                userToSave.setGithubName(gitHubNameBox.getText());

                administrationService.editUser(userToSave, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        ExceptionHandler.handle("Couldn't save user.", caught);
                    }

                    @Override
                    public void onSuccess(Void result) {
                        if (user != null) {
                            user.setName(userToSave.getName());
                            user.setGithubName(userToSave.getGithubName());
                        }
                        onSave(userToSave);
                        EditUserDialog.this.hide();
                        EditUserDialog.this.removeFromParent();
                    }
                });
            }
        });

        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                EditUserDialog.this.hide();
                EditUserDialog.this.removeFromParent();
            }
        });
    }

    protected void onSave(KerberosUser savedUser) {}
}
