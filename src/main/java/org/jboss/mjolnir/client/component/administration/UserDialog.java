package org.jboss.mjolnir.client.component.administration;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.mjolnir.shared.domain.KerberosUser;
import org.jboss.mjolnir.client.ExceptionHandler;
import org.jboss.mjolnir.client.component.util.HTMLUtil;
import org.jboss.mjolnir.shared.domain.EntityUpdateResult;
import org.jboss.mjolnir.client.service.AdministrationService;
import org.jboss.mjolnir.client.service.AdministrationServiceAsync;

/**
 * Dialog for modifying users.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class UserDialog extends DialogBox {

    interface Binder extends UiBinder<Widget, UserDialog> {
    }

    private static Binder uiBinder = GWT.create(Binder.class);

    private AdministrationServiceAsync administrationService = AdministrationService.Util.getInstance();

    @UiField
    TextBox kerberosNameBox;

    @UiField
    TextBox gitHubNameBox;

    @UiField
    CheckBox adminCheckBox;

    @UiField
    CheckBox whitelistedCheckBox;

    @UiField
    CheckBox activeAccountCheckBox;

    @UiField
    Button submitButton;

    @UiField
    Button cancelButton;

    @UiField
    HTML feedbackLabel;

    public UserDialog(final KerberosUser user, DialogType dialogType) {
        setGlassEnabled(true);
        setWidget(uiBinder.createAndBindUi(this));

        //the value of the activeCheckBox cannot be changed
        activeAccountCheckBox.setEnabled(false);

        switch (dialogType) {
            case EDIT:
                initEditDialog(user);
                break;
            case REGISTER:
                initRegisterDialog();
                break;
        }
    }

    public enum DialogType {
        REGISTER,
        EDIT
    }

    protected void onSave(KerberosUser savedUser) {
    }

    private void setActiveKrbAccount(String uid) {
        //check whether the account is active kerberos
        administrationService.checkUserExists(uid, new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable caught) {
                ExceptionHandler.handle("Couldn't locate whether the user has an active krb account.", caught);
            }

            @Override
            public void onSuccess(Boolean result) {
                activeAccountCheckBox.setValue(result);
            }
        });
    }

    private void initEditDialog(final KerberosUser user) {
        setText("Edit User");

        if (user != null && user.getName() != null) { // username is not modifiable for existing users
            kerberosNameBox.setEnabled(false);
        } else {                                        //GH name cannot be modified with null kerberos
            setKerberosNameBoxActiveValidation();
            gitHubNameBox.setEnabled(false);
        }

        // set field values
        if (user != null) {
            kerberosNameBox.setText(user.getName());
            gitHubNameBox.setText(user.getGithubName());
            adminCheckBox.setValue(user.isAdmin());
            whitelistedCheckBox.setValue(user.isWhitelisted());
            setActiveKrbAccount(user.getName());
        }


        submitButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // create new user instance, so we don't modify original instance with data that may not be accepted
                final KerberosUser userToSave;
                if (user != null) {
                    userToSave = user.copy();
                } else {
                    userToSave = new KerberosUser();
                }

                // set updated data
                //krb name must be null, when not entered because of the unique constraint
                String userName = kerberosNameBox.getText();
                userToSave.setName(userName.isEmpty() ? null : userName);

                if (userToSave.getGithubName().equals(gitHubNameBox.getText())) {
                    gitHubNameBox.setEnabled(false);
                } else {
                    userToSave.setGithubName(gitHubNameBox.getText());
                }

                userToSave.setAdmin(adminCheckBox.getValue());
                userToSave.setWhitelisted(whitelistedCheckBox.getValue());

                // save
                administrationService.editUser(userToSave, kerberosNameBox.isEnabled(), gitHubNameBox.isEnabled(), new AsyncCallback<EntityUpdateResult<KerberosUser>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        ExceptionHandler.handle("Couldn't save user.", caught);
                    }

                    @Override
                    public void onSuccess(EntityUpdateResult<KerberosUser> result) {
                        if (result.isOK()) {
                            if (user != null) {
                                user.setName(userToSave.getName());
                                user.setGithubName(userToSave.getGithubName());
                            }
                            onSave(userToSave);
                            UserDialog.this.hide();
                            UserDialog.this.removeFromParent();
                        } else {
                            feedbackLabel.setHTML(HTMLUtil.toUl(result.getValidationMessages()));
                        }
                    }
                });
            }
        });

        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                UserDialog.this.hide();
                UserDialog.this.removeFromParent();
            }
        });
    }

    private void initRegisterDialog() {
        setText("Register User");

        setKerberosNameBoxActiveValidation();

        submitButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // create new user instance
                final KerberosUser user = new KerberosUser();

                //krb name must be null, when not entered because of the unique constraint
                String userName = kerberosNameBox.getText();

                user.setName(userName.isEmpty() ? null : userName);
                user.setGithubName(gitHubNameBox.getText());
                user.setAdmin(adminCheckBox.getValue());
                user.setWhitelisted(whitelistedCheckBox.getValue());

                // save
                administrationService.registerUser(user, new AsyncCallback<EntityUpdateResult<KerberosUser>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        ExceptionHandler.handle("Couldn't save user.", caught);
                    }

                    @Override
                    public void onSuccess(EntityUpdateResult<KerberosUser> result) {
                        if (result.isOK()) {
                            if (user != null) {
                                user.setName(user.getName());
                                user.setGithubName(user.getGithubName());
                            }
                            onSave(user);
                            UserDialog.this.hide();
                            UserDialog.this.removeFromParent();
                        } else {
                            feedbackLabel.setHTML(HTMLUtil.toUl(result.getValidationMessages()));
                        }
                    }
                });
            }
        });

        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                UserDialog.this.hide();
                UserDialog.this.removeFromParent();
            }
        });
    }

    private void setKerberosNameBoxActiveValidation() {
        kerberosNameBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                setActiveKrbAccount(kerberosNameBox.getText());
            }
        });
    }
}
