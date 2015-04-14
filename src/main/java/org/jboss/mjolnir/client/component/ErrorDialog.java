package org.jboss.mjolnir.client.component;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.logging.impl.StackTracePrintStream;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * Error dialog.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class ErrorDialog extends DialogBox {

    public ErrorDialog(Throwable throwable) {
        this(null, throwable);
    }

    public ErrorDialog(String message, final Throwable throwable) {
        setText("Error!");

        final HTMLPanel panel = new HTMLPanel("");
        panel.getElement().getStyle().setBorderWidth(1, Style.Unit.PX);
        setWidget(panel);

        // heading

        panel.add(new HTMLPanel("h3", "Something went terribly wrong:"));


        // message

        final HTMLPanel messagePara = new HTMLPanel("p", message);
        messagePara.setStyleName("strongText");
        panel.add(messagePara);

        String reason = "Reason: " + throwable.getMessage();
        if (throwable.getCause() != null) {
            reason += ": " + throwable.getCause().getMessage();
        }
        final HTMLPanel reasonPara = new HTMLPanel("p", reason);
        panel.add(reasonPara);


        // stack trace para

        final HTMLPanel stacktracePara = new HTMLPanel("p", "");
        final Anchor showStackAnchor = new Anchor("Show stacktrace");
        showStackAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                StringBuilder sb = new StringBuilder();
                throwable.printStackTrace(new StackTracePrintStream(sb));
                stacktracePara.add(new HTMLPanel("pre", sb.toString()));
                showStackAnchor.removeFromParent();
            }
        });
        stacktracePara.add(showStackAnchor);
        panel.add(stacktracePara);


        // continue button

        final HTMLPanel buttonPanel = new HTMLPanel("p", "");
        buttonPanel.setStyleName("textRight");
        panel.add(buttonPanel);

        final Button continueButton = new Button("Continue");
        continueButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ErrorDialog.this.hide();
                ErrorDialog.this.removeFromParent();
            }
        });
        buttonPanel.add(continueButton);
    }
}
