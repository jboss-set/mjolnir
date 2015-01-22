package org.jboss.mjolnir.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.mjolnir.client.component.SubscriptionScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class LayoutPanel extends Composite {

    private Logger logger = Logger.getLogger("");

    interface Binder extends UiBinder<Widget, LayoutPanel> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    private LoginServiceAsync loginService = LoginService.Util.getInstance();

    @UiField
    VerticalPanel menuPanel;

    @UiField
    HTMLPanel mainPanel;

    @UiField
    Anchor logoutLink;

    private List<Anchor> menuLinks = new ArrayList<Anchor>();

    public LayoutPanel() {
        initWidget(uiBinder.createAndBindUi(this));

        logoutLink.setHref("#");
        logoutLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                loginService.logout(new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        logger.log(Level.SEVERE, caught.getMessage(), caught);
                    }

                    @Override
                    public void onSuccess(Void result) {
                        EntryPage.getInstance().goToLoginScreen();
                    }
                });
            }
        });

        addLink(new Anchor("GitHub teams subscription"), new MenuClickHandler(new SubscriptionScreen()));
        addLink(new Anchor("Change GitHub name"), new MenuClickHandler(new Label("GitHub name setting")));
    }

    private void addLink(Anchor link, MenuClickHandler clickHandler) {
        link.addClickHandler(clickHandler);
        clickHandler.setCurrentLink(link);
        menuLinks.add(link);
        menuPanel.add(link);
    }

    private class MenuClickHandler implements ClickHandler {

        private Widget widget;
        private Anchor currentLink;

        private MenuClickHandler(Widget widget) {
            this.widget = widget;
        }

        public void setCurrentLink(Anchor link) {
            currentLink = link;
        }

        @Override
        public void onClick(ClickEvent event) {
            for (Anchor link: menuLinks) {
                link.removeStyleName("active");
            }
            currentLink.setStyleName("active");
            mainPanel.clear();
            mainPanel.add(widget);
        }
    }
}
