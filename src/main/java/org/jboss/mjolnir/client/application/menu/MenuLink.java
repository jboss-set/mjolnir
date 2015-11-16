package org.jboss.mjolnir.client.application.menu;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.InlineHyperlink;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
class MenuLink extends SimplePanel {
    public MenuLink(String title, String nameToken) {
        super(Document.get().createLIElement());
        InlineHyperlink link = new InlineHyperlink(title, nameToken);
        getElement().setInnerHTML(link.getElement().getString());
    }

}
