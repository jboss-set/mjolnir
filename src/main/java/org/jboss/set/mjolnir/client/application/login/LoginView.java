package org.jboss.set.mjolnir.client.application.login;

import com.google.inject.Inject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class LoginView extends ViewWithUiHandlers<LoginHandlers> implements LoginPresenter.MyView {

    interface Binder extends UiBinder<Widget, LoginView> {}

    @UiField
    FormPanel form;

    @UiField
    TextBox usernameField;

    @UiField
    PasswordTextBox passwordField;

    @UiField
    Label feedbackLabel;

    @Inject
    public LoginView(Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            public void execute() {
                usernameField.setFocus(true);
            }
        });
    }

    @UiHandler("form")
    public void onLogin(FormPanel.SubmitEvent event) {
        String name = usernameField.getText();
        String password = passwordField.getText();
        getUiHandlers().login(name, password);
    }

    @Override
    public void setFeedbackMessage(String message) {
        feedbackLabel.setText(message);
        passwordField.setText("");
    }

    @Override
    public void reset() {
        usernameField.setText("");
        passwordField.setText("");
        setFeedbackMessage("");
    }
}
