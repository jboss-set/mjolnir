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
import org.jboss.mjolnir.client.ExceptionHandler;
import org.jboss.mjolnir.client.component.administration.RegisteredUsersScreen;
import org.jboss.mjolnir.client.component.administration.SubscriptionSummaryScreen;
import org.jboss.mjolnir.client.domain.ReportType;
import org.jboss.mjolnir.client.service.LoginService;
import org.jboss.mjolnir.client.service.LoginServiceAsync;

import java.util.ArrayList;
import java.util.List;

/**
 * Main panel defining UI layout.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class LayoutPanel extends Composite {

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
                        ExceptionHandler.handle(caught);
                    }

                    @Override
                    public void onSuccess(Void result) {
                        EntryPage.getInstance().goToLoginScreen();
                    }
                });
            }
        });

        usernameLabel.setText(CurrentUser.get().getName());

        // create menu items

        addMenuHeading("Your GitHub Settings");
        addMenuLink(new Anchor("GitHub Subscriptions"), new MenuClickHandler() {
            @Override
            public Widget createWidget() {
                return new SubscriptionScreen();
            }
        });

        if (CurrentUser.get().isAdmin()) {
            addMenuHeading("Administration");
            addMenuLink(new Anchor("GitHub Organization Members"), new MenuClickHandler() {
                @Override
                public Widget createWidget() {
                    return new SubscriptionSummaryScreen();
                }
            });
            addMenuLink(new Anchor("Registered Users"), new MenuClickHandler() {
                @Override
                public Widget createWidget() {
                    return new RegisteredUsersScreen();
                }
            });

            addMenuHeading("Reports");
            addMenuLink(new Anchor("Unknown Members"), new MenuClickHandler() {
                @Override
                public Widget createWidget() {
                    return new ReportScreen(ReportType.UNKNOWN_MEMBERS, "Unknown GitHub Members");
                }
            });
        }

        mainPanel.add(new SubscriptionScreen());
        menuLinks.get(0).setStyleName("active"); // set first link as active
    }

    private void addMenuHeading(String name) {
        final HTMLPanel heading = new HTMLPanel("div", name);
        heading.setStyleName("heading");
        menuPanel.add(heading);
    }

    private void addMenuLink(Anchor link, MenuClickHandler clickHandler) {
        link.addClickHandler(clickHandler);
        clickHandler.setCurrentLink(link);
        menuLinks.add(link);
        menuPanel.add(link);
    }

    private abstract class MenuClickHandler implements ClickHandler {

        private Anchor currentLink;

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
            mainPanel.add(createWidget());
        }

        public abstract Widget createWidget();

    }
}
