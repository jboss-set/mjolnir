package org.jboss.mjolnir.client.application.menu;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.InlineHyperlink;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class MenuLink extends SimplePanel {

    private String nameToken;

    public MenuLink(String title, String nameToken) {
        super(Document.get().createLIElement());
        InlineHyperlink link = new InlineHyperlink(title, nameToken);
        setWidget(link);
        this.nameToken = nameToken;
    }

    public String getNameToken() {
        return nameToken;
    }
}
