package org.jboss.set.mjolnir.client.application;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.jboss.set.mjolnir.client.application.menu.Menu;
import org.jboss.set.mjolnir.client.application.security.IsAdminGatekeeper;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class ApplicationView extends ViewWithUiHandlers<ApplicationUiHandlers> implements ApplicationPresenter.MyView {

    interface Binder extends UiBinder<Widget, ApplicationView> {}

    @UiField
    HTMLPanel mainPanel;

    @UiField
    InlineLabel usernameLabel;

    @UiField
    DockLayoutPanel rootPanel;

    @UiField
    Anchor logoutLink;

    @UiField(provided = true)
    Menu mainMenu;

    @UiField(provided = true)
    Menu adminMenu;

    @Inject
    public ApplicationView(Binder binder, IsAdminGatekeeper gatekeeper) {
        this.mainMenu = new Menu(ApplicationPresenter.MAIN_MENU);
        this.adminMenu = new Menu(ApplicationPresenter.ADMIN_MENU, gatekeeper);

        initWidget(binder.createAndBindUi(this));

        rootPanel.getElement().getStyle().clearPosition();

        bindSlot(ApplicationPresenter.SLOT_CONTENT, mainPanel);
    }

    @Override
    public void setUsername(String username) {
        usernameLabel.setText(username);
    }

    @UiHandler("logoutLink")
    public void onLogoutLinkClicked(ClickEvent clickEvent) {
        getUiHandlers().logout();
    }
}
