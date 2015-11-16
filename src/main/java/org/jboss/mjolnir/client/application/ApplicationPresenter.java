package org.jboss.mjolnir.client.application;

import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.presenter.slots.NestedSlot;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.mjolnir.client.NameTokens;
import org.jboss.mjolnir.client.application.security.CurrentUser2;
import org.jboss.mjolnir.client.service.DefaultCallback;
import org.jboss.mjolnir.client.service.LoginService;
import org.jboss.mjolnir.client.service.LoginServiceAsync;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class ApplicationPresenter extends Presenter<ApplicationPresenter.MyView, ApplicationPresenter.MyProxy>
        implements ApplicationUiHandlers {

    interface MyView extends View, HasUiHandlers<ApplicationUiHandlers> {
        void setUsername(String username);
    }

    @ProxyStandard
    interface MyProxy extends Proxy<ApplicationPresenter> {}

    public static final NestedSlot SLOT_CONTENT = new NestedSlot();

    private Logger logger = Logger.getLogger(getClass().getName());

    private CurrentUser2 currentUser;
    private PlaceManager placeManager;

    @Inject
    public ApplicationPresenter(EventBus eventBus, MyView view, MyProxy proxy, PlaceManager placeManager,
                                CurrentUser2 currentUser) {
        super(eventBus, view, proxy, RevealType.Root);

        this.currentUser = currentUser;
        this.placeManager = placeManager;

        getView().setUiHandlers(this);
        getView().setUsername(currentUser.getUser().getName());
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
                redirectToLoginPage();
            }
        });
    }

    private void redirectToLoginPage() {
        PlaceRequest placeRequest = new PlaceRequest.Builder()
                .nameToken(NameTokens.getOnLoginDefaultPage())
                .build();
        placeManager.revealPlace(placeRequest);
    }

}
