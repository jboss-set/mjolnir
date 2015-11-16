package org.jboss.mjolnir.client.application.menu;

import javax.inject.Inject;

import com.google.gwt.dom.client.UListElement;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import org.jboss.mjolnir.client.NameTokens;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class MainMenu extends Composite {

    @Inject
    public MainMenu() {
        FlowPanel panel = new FlowPanel();
        initWidget(panel);

        FlowPanel list = new FlowPanel(UListElement.TAG);
        list.add(new MenuLink("Subscriptions", NameTokens.MY_SUBSCRIPTIONS));
        panel.add(list);
    }

}
