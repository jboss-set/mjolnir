package org.jboss.mjolnir.client.component;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.mjolnir.client.CurrentUser;
import org.jboss.mjolnir.client.EntryPage;
import org.jboss.mjolnir.client.service.LoginService;
import org.jboss.mjolnir.client.service.LoginServiceAsync;

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

    @UiField
    VerticalPanel menuPanel;

    @UiField
    HTMLPanel mainPanel;

    @UiField
    Anchor logoutLink;

    @UiField
    InlineLabel usernameLabel;

    private List<Anchor> menuLinks = new ArrayList<Anchor>();
    private LoginServiceAsync loginService = LoginService.Util.getInstance();
    private Widget subscriptionScreen = new SubscriptionScreen();

    public LayoutPanel() {
        initWidget(uiBinder.createAndBindUi(this));

        final Style style = getStyleElement().getStyle();
        style.setProperty("margin", "2px auto");
        style.setProperty("background", "white");

        logoutLink.setHref("javascript:;");
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

        usernameLabel.setText(CurrentUser.get().getName());

        addLink(new Anchor("Subscribe to GitHub teams"), new MenuClickHandler(subscriptionScreen));
        addLink(new Anchor("Test Page"), new MenuClickHandler(new HTMLPanel("h2", "Test Page")));
        if (CurrentUser.get().isAdmin()) {
            addLink(new Anchor("Administration"), new MenuClickHandler(subscriptionScreen));
        }

        mainPanel.add(subscriptionScreen);
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
                currentLink.setEnabled(true);
            }
            currentLink.setStyleName("active");
            currentLink.setEnabled(false);
            mainPanel.clear();
            mainPanel.add(widget);
        }
    }
}
