package org.jboss.mjolnir.client.component;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * Authentication time-out dialog.
 *
 * Redirects user to login page.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class AuthenticationTimedOutDialog extends DialogBox {

    public AuthenticationTimedOutDialog() {
        setText("Not authenticated");

        final HTMLPanel panel = new HTMLPanel("");
        panel.getElement().getStyle().setBorderWidth(1, Style.Unit.PX);
        setWidget(panel);

        // heading

        panel.add(new HTMLPanel("p", "Authentication timed out."));

        // continue button

        final HTMLPanel buttonPanel = new HTMLPanel("p", "");
        buttonPanel.setStyleName("textRight");
        panel.add(buttonPanel);

        final Button button = new Button("Login");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.Location.reload();
            }
        });
        buttonPanel.add(button);
    }

}
