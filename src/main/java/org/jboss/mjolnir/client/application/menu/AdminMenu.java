package org.jboss.mjolnir.client.application.menu;

import javax.inject.Inject;

import com.google.gwt.dom.client.UListElement;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import org.jboss.mjolnir.client.NameTokens;
import org.jboss.mjolnir.client.application.security.IsAdminGatekeeper;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
@SuppressWarnings("unused") // ApplicationView.ui.xml
public class AdminMenu extends Composite {

    private IsAdminGatekeeper gatekeeper;

    @Inject
    public AdminMenu(IsAdminGatekeeper gatekeeper) {
        this.gatekeeper = gatekeeper;

        FlowPanel panel = new FlowPanel();
        initWidget(panel);

        final HTMLPanel heading = new HTMLPanel("div", "Administration");
        heading.setStyleName("heading");
        panel.add(heading);

        FlowPanel list = new FlowPanel(UListElement.TAG);
        list.add(new MenuLink("GitHub Organization Members", NameTokens.GITHUB_MEMBERS));
        panel.add(list);
    }

    @Override
    protected void onLoad() {
        setVisible(gatekeeper.canReveal());
    }

}
