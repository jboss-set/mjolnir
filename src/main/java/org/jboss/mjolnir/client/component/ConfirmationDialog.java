package org.jboss.mjolnir.client.component;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHTML;

/**
 * General confirmation dialog.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public abstract class ConfirmationDialog extends DialogBox {

    /**
     * @param message message to show
     */
    protected ConfirmationDialog(String message) {
        this(message, null);
    }

    /**
     * @param message message to show
     * @param additionalMessage optional more detailed message
     */
    public ConfirmationDialog(String message, String additionalMessage) {
        setText("Confirmation");
        setGlassEnabled(true);

        final HTMLPanel panel = new HTMLPanel("");
        panel.setStyleName("padding");
        setWidget(panel);

        final HTMLPanel messagePara = new HTMLPanel("p", message);
        messagePara.setStyleName("strongText");
        panel.add(messagePara);

        if (additionalMessage != null) {
            panel.add(new HTMLPanel("p", additionalMessage));
        }

        final HTMLPanel buttonPanel = new HTMLPanel("p", "");
        buttonPanel.setStyleName("textRight");
        panel.add(buttonPanel);

        final Button okButton = new Button("OK");
        okButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onConfirm();
                ConfirmationDialog.this.hide();
                ConfirmationDialog.this.removeFromParent();
            }
        });
        buttonPanel.add(okButton);
        buttonPanel.add(new InlineHTML(" "));

        final Button cancelButton = new Button("Cancel");
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ConfirmationDialog.this.hide();
                ConfirmationDialog.this.removeFromParent();
            }
        });
        buttonPanel.add(cancelButton);
    }

    /**
     * Called when user confirms the action.
     */
    public abstract void onConfirm();
}
