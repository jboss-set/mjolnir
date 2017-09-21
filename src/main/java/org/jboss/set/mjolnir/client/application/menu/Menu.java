package org.jboss.set.mjolnir.client.application.menu;

import java.util.List;

import com.google.gwt.dom.client.UListElement;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.gwtplatform.mvp.client.proxy.Gatekeeper;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class Menu extends Composite {

    private Gatekeeper gatekeeper;

    public Menu(List<MenuLink> navigationItems) {
        this(navigationItems, null);
    }

    public Menu(List<MenuLink> navigationItems, Gatekeeper gatekeeper) {
        this.gatekeeper = gatekeeper;

        FlowPanel panel = new FlowPanel();
        initWidget(panel);

        final HTMLPanel heading = new HTMLPanel("div", "Administration");
        heading.setStyleName("heading");
        panel.add(heading);

        FlowPanel list = new FlowPanel(UListElement.TAG);
        panel.add(list);

        for (MenuLink menuItem: navigationItems) {
            list.add(menuItem);
        }
    }

    @Override
    protected void onLoad() {
        setVisible(gatekeeper == null || gatekeeper.canReveal());
    }

}
