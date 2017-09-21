package org.jboss.set.mjolnir.client.component;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.set.mjolnir.client.component.util.HTMLUtil;

/**
 * Popup allowing user to change his GitHub name
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public abstract class ModifyGitHubNamePopup extends PopupPanel {

    interface Binder extends UiBinder<Widget, ModifyGitHubNamePopup> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    @UiField
    Label messageLabel;

    @UiField
    TextBox textBox;

    @UiField
    Button submitButton;

    @UiField
    Button cancelButton;

    @UiField
    HTML feedbackLabel;


    public ModifyGitHubNamePopup() {
        super(false); // no auto hide
        setGlassEnabled(true); // forbid to click outside the popup
        setWidget(uiBinder.createAndBindUi(this));

        messageLabel.setText("GitHub Username");

        submitButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                submitButton.setEnabled(false);
                onSubmit(textBox.getText());
            }
        });

        cancelButton.addClickHandler(new CancelClickHandler());
    }

    @Override
    public void center() {
        feedbackLabel.setText("");

        super.center();
    }

    public void setUsername(String username) {
        textBox.setText(username);
        cancelButton.setEnabled(username != null);
    }

    /**
     * Closes the dialog an re-enables save button
     */
    public void success() {
        hide();
        submitButton.setEnabled(true);
    }

    /**
     * Displays validation messages and re-enables save button
     *
     * @param messages validation messages
     */
    public void validationError(List<String> messages) {
        feedbackLabel.setHTML(HTMLUtil.toUl(messages));
        submitButton.setEnabled(true);
    }

    public void enableCancelButton(boolean enabled) {
        cancelButton.setEnabled(enabled);
    }

    /**
     * Called when users saves new username
     *
     * Call {@link #success()} or {@link #validationError(List)} when result is available.
     *
     * @param newUsername
     */
    public abstract void onSubmit(String newUsername);

    private class CancelClickHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            ModifyGitHubNamePopup.this.hide();
        }
    }

}
