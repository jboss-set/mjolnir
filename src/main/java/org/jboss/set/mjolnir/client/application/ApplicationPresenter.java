package org.jboss.set.mjolnir.client.application;

import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.presenter.slots.NestedSlot;
import com.gwtplatform.mvp.client.proxy.NavigationEvent;
import com.gwtplatform.mvp.client.proxy.NavigationHandler;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.Proxy;
import org.jboss.set.mjolnir.client.NameTokens;
import org.jboss.set.mjolnir.client.application.menu.MenuLink;
import org.jboss.set.mjolnir.client.application.security.CurrentUser;
import org.jboss.set.mjolnir.client.service.DefaultCallback;
import org.jboss.set.mjolnir.client.service.LoginService;
import org.jboss.set.mjolnir.client.service.LoginServiceAsync;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class ApplicationPresenter extends Presenter<ApplicationPresenter.MyView, ApplicationPresenter.MyProxy>
        implements ApplicationUiHandlers, NavigationHandler {

    interface MyView extends View, HasUiHandlers<ApplicationUiHandlers> {
        void setUsername(String username);
    }

    @ProxyStandard
    interface MyProxy extends Proxy<ApplicationPresenter> {}

    public static final NestedSlot SLOT_CONTENT = new NestedSlot();
    public static List<MenuLink> MAIN_MENU;
    public static List<MenuLink> ADMIN_MENU;

    static {
        MAIN_MENU = new ArrayList<>();
        MAIN_MENU.add(new MenuLink("Subscriptions", NameTokens.MY_SUBSCRIPTIONS));

        ADMIN_MENU = new ArrayList<>();
        ADMIN_MENU.add(new MenuLink("GitHub Organization Members", NameTokens.GITHUB_MEMBERS));
        ADMIN_MENU.add(new MenuLink("Registered Users", NameTokens.REGISTERED_USERS));
    }


    private Logger logger = Logger.getLogger(getClass().getName());

    private CurrentUser currentUser;
    private PlaceManager placeManager;

    @Inject
    public ApplicationPresenter(EventBus eventBus, MyView view, MyProxy proxy, PlaceManager placeManager,
                                CurrentUser currentUser) {
        super(eventBus, view, proxy, RevealType.Root);

        this.currentUser = currentUser;
        this.placeManager = placeManager;

        getView().setUiHandlers(this);
        getView().setUsername(currentUser.getUser().getKrbName());
    }

    @Override
    public void logout() {
        logger.info("Logging out.");
        LoginServiceAsync loginService = LoginService.Util.getInstance();
        loginService.logout(new DefaultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                logger.info("Log out successful.");
                currentUser.reset();
                Window.Location.replace("logout.html?GLO=true");
            }
        });
    }

    @Override
    protected void onBind() {
        addRegisteredHandler(NavigationEvent.getType(), this);
        selectMenuItemByToken(MAIN_MENU, NameTokens.MY_SUBSCRIPTIONS);
    }

    @Override
    public void onNavigation(NavigationEvent navigationEvent) {
        String nameToken = navigationEvent.getRequest().getNameToken();
        selectMenuItemByToken(MAIN_MENU, nameToken);
        selectMenuItemByToken(ADMIN_MENU, nameToken);
    }

    private void selectMenuItemByToken(List<MenuLink> menuItems, String nameToken) {
        for (MenuLink menuLink: menuItems) {
            if (menuLink.getNameToken().equals(nameToken)) {
                menuLink.getWidget().getElement().setClassName("active");
            } else {
                menuLink.getWidget().getElement().setClassName("");
            }
        }
    }

}
