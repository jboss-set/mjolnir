package org.jboss.mjolnir.client.component.administration;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.jboss.mjolnir.authentication.KerberosUser;
import org.jboss.mjolnir.client.ExceptionHandler;
import org.jboss.mjolnir.client.component.util.HTMLUtil;
import org.jboss.mjolnir.client.domain.EntityUpdateResult;
import org.jboss.mjolnir.client.service.AdministrationService;
import org.jboss.mjolnir.client.service.AdministrationServiceAsync;

/**
 * @author Martin Stefanko (mstefank@redhat.com)
 */
public class RegisterUserDialog extends DialogBox {

    //TODO merge edit and registration dialog
    //TODO add active account check box
    //TODO add validation on exit of the field with kerberos name


    interface Binder extends UiBinder<Widget, RegisterUserDialog> {}
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
    Button submitButton;

    @UiField
    Button cancelButton;

    @UiField
    HTML feedbackLabel;

    public RegisterUserDialog() {
        setGlassEnabled(true);
        setWidget(uiBinder.createAndBindUi(this));
        setText("Register User");

        submitButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // create new user instance
                final KerberosUser user = new KerberosUser();

                user.setName(kerberosNameBox.getText());
                user.setGithubName(gitHubNameBox.getText());
                user.setAdmin(adminCheckBox.getValue());
                user.setWhitelisted(whitelistedCheckBox.getValue());

                // save
                administrationService.registerUser(user, new AsyncCallback<EntityUpdateResult<KerberosUser>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        ExceptionHandler.handle("Couldn't save user. Check whether the kerberos username is unique.", caught);
                    }

                    @Override
                    public void onSuccess(EntityUpdateResult<KerberosUser> result) {
                        if (result.isOK()) {
                            if (user != null) {
                                user.setName(user.getName());
                                user.setGithubName(user.getGithubName());
                            }
                            onSave(user);
                            RegisterUserDialog.this.hide();
                            RegisterUserDialog.this.removeFromParent();
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
                RegisterUserDialog.this.hide();
                RegisterUserDialog.this.removeFromParent();
            }
        });
    }

    protected void onSave(KerberosUser savedUser) {}
}
