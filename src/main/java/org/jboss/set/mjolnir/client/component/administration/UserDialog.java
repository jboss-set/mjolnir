package org.jboss.set.mjolnir.client.component.administration;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.set.mjolnir.shared.domain.RegisteredUser;
import org.jboss.set.mjolnir.client.ExceptionHandler;
import org.jboss.set.mjolnir.client.component.util.HTMLUtil;
import org.jboss.set.mjolnir.shared.domain.EntityUpdateResult;
import org.jboss.set.mjolnir.client.service.AdministrationService;
import org.jboss.set.mjolnir.client.service.AdministrationServiceAsync;

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
    TextBox noteBox;

    @UiField
    TextBox responsiblePersonBox;

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

    public UserDialog(final RegisteredUser user, DialogType dialogType) {
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

    protected void onSave(RegisteredUser savedUser, boolean activeAccount) {
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

    private void initEditDialog(final RegisteredUser user) {
        setText("Edit User");

        if (user != null && user.getKrbName() != null) { // username is not modifiable for existing users
            kerberosNameBox.setEnabled(false);
        } else {                                        //GH name cannot be modified with null kerberos
            setKerberosNameBoxActiveValidation();
            gitHubNameBox.setEnabled(false);
        }

        // set field values
        if (user != null) {
            kerberosNameBox.setText(user.getKrbName());
            gitHubNameBox.setText(user.getGitHubName());
            noteBox.setText(user.getNote());
            adminCheckBox.setValue(user.isAdmin());
            whitelistedCheckBox.setValue(user.isWhitelisted());
            responsiblePersonBox.setValue(user.getResponsiblePerson());
            setActiveKrbAccount(user.getKrbName());
        }


        submitButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // create new user instance, so we don't modify original instance with data that may not be accepted
                final RegisteredUser userToSave;
                if (user != null) {
                    userToSave = user.copy();
                } else {
                    userToSave = new RegisteredUser();
                }

                // set updated data
                //krb name must be null, when not entered because of the unique constraint
                String userName = kerberosNameBox.getText();
                userToSave.setKrbName(userName.isEmpty() ? null : userName);

                if (userToSave.getGitHubName().equals(gitHubNameBox.getText())) {
                    gitHubNameBox.setEnabled(false);
                } else {
                    userToSave.setGitHubName(gitHubNameBox.getText());
                }

                userToSave.setAdmin(adminCheckBox.getValue());
                userToSave.setWhitelisted(whitelistedCheckBox.getValue());
                userToSave.setResponsiblePerson(responsiblePersonBox.getValue());

                // save
                administrationService.editUser(userToSave, kerberosNameBox.isEnabled(), gitHubNameBox.isEnabled(), new AsyncCallback<EntityUpdateResult<RegisteredUser>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        ExceptionHandler.handle("Couldn't save user.", caught);
                    }

                    @Override
                    public void onSuccess(EntityUpdateResult<RegisteredUser> result) {
                        if (result.isOK()) {
                            if (user != null) {
                                user.setKrbName(userToSave.getKrbName());
                                user.setGitHubName(userToSave.getGitHubName());
                            }
                            onSave(userToSave, activeAccountCheckBox.getValue());
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

        responsiblePersonBox.getElement().setId("responsiblePersonBox");
        responsiblePersonBox.setEnabled(whitelistedCheckBox.getValue());

        whitelistedCheckBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                CheckBox checkBox = (CheckBox)event.getSource();

                if (checkBox.getValue()) {
                    DOM.getElementById("responsiblePersonBox").removeAttribute("disabled");
                } else {
                    Element responsiblePersonElement = DOM.getElementById("responsiblePersonBox");
                    responsiblePersonElement.setAttribute("disabled", "disabled");
                    TextBox.wrap(responsiblePersonElement).setValue("");
                }
           }
        });

        setKerberosNameBoxActiveValidation();

        submitButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // create new user instance
                final RegisteredUser user = new RegisteredUser();

                //krb name must be null, when not entered because of the unique constraint
                String userName = kerberosNameBox.getText();

                user.setKrbName(userName.isEmpty() ? null : userName);
                user.setGitHubName(gitHubNameBox.getText());
                user.setNote(noteBox.getText());
                user.setAdmin(adminCheckBox.getValue());
                user.setWhitelisted(whitelistedCheckBox.getValue());
                user.setResponsiblePerson(responsiblePersonBox.getValue());

                // save
                administrationService.registerUser(user, new AsyncCallback<EntityUpdateResult<RegisteredUser>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        ExceptionHandler.handle("Couldn't save user.", caught);
                    }

                    @Override
                    public void onSuccess(EntityUpdateResult<RegisteredUser> result) {
                        if (result.isOK()) {
                            RegisteredUser savedUser = result.getUpdatedEntity();
                            if (savedUser != null) {
                                user.setKrbName(savedUser.getKrbName());
                                user.setGitHubName(savedUser.getGitHubName());
                            }
                            onSave(user, activeAccountCheckBox.getValue());
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
