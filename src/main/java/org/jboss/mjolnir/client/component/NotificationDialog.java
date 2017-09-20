package org.jboss.mjolnir.client.component;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * Shows a popup with a message and an OK button.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class NotificationDialog extends DialogBox {

    public NotificationDialog(String caption, String message) {
        this(caption, SafeHtmlUtils.fromString(message));
    }

    public NotificationDialog(String caption, SafeHtml message) {
        setText(caption);

        HTMLPanel mainPanel = new HTMLPanel("");
        setWidget(mainPanel);

        mainPanel.add(new HTMLPanel(message));

        Button button = new Button("OK");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                NotificationDialog.this.hide();
                NotificationDialog.this.removeFromParent();
            }
        });
        HTMLPanel buttonPanel = new HTMLPanel("p", "");
        buttonPanel.add(button);
        mainPanel.add(buttonPanel);
    }
}
