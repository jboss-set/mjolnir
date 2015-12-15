package org.jboss.mjolnir.client.application.login;

import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.NoGatekeeper;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.mjolnir.client.NameTokens;
import org.jboss.mjolnir.client.XsrfUtil;
import org.jboss.mjolnir.client.application.security.CurrentUser;
import org.jboss.mjolnir.client.component.ProcessingIndicatorPopup;
import org.jboss.mjolnir.client.service.DefaultCallback;
import org.jboss.mjolnir.client.service.LoginService;
import org.jboss.mjolnir.client.service.LoginServiceAsync;
import org.jboss.mjolnir.shared.domain.KerberosUser;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class LoginPresenter extends Presenter<LoginPresenter.MyView, LoginPresenter.MyProxy>
        implements LoginHandlers {

    interface MyView extends View, HasUiHandlers<LoginHandlers> {
        void setFeedbackMessage(String message);

        void reset();
    }

    @ProxyStandard
    @NameToken(NameTokens.LOGIN)
    @NoGatekeeper
    interface MyProxy extends ProxyPlace<LoginPresenter> {
    }

    private CurrentUser currentUser;
    private PlaceManager placeManager;

    @Inject
    public LoginPresenter(EventBus eventBus, MyView view, MyProxy proxy, PlaceManager placeManager,
                          CurrentUser currentUser) {
        super(eventBus, view, proxy, RevealType.Root);
        getView().setUiHandlers(this);

        this.currentUser = currentUser;
        this.placeManager = placeManager;
    }

    @Override
    public void login(final String username, final String password) {
        LoginServiceAsync loginService = LoginService.Util.getInstance();
        loginService.login(username, password, new DefaultCallback<KerberosUser>() {
            @Override
            public void onSuccess(KerberosUser user) {
                currentUser.setUser(user);

                if (currentUser.isLoggedIn()) {
                    redirectToHomePage();
                    getView().reset();
                } else {
                    getView().setFeedbackMessage("Wrong credentials.");
                }
            }
        });
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }

    // TODO: is there better way to determine whether user is already authenticated? This is sometimes causing
    // exceptions due to cancelled RPC calls
    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);

        XsrfUtil.obtainToken(new XsrfUtil.Callback() {
            @Override
            public void onSuccess(XsrfToken token) {
                LoginServiceAsync loginService = LoginService.Util.getInstance();
                loginService.getLoggedUser(new DefaultCallback<KerberosUser>() {
                    @Override
                    public void onSuccess(KerberosUser result) {
                        currentUser.setUser(result);
                        if (currentUser.isLoggedIn()) {
                            redirectToHomePage();
                        }
                        getProxy().manualReveal(LoginPresenter.this);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        super.onFailure(caught);
                        getProxy().manualRevealFailed();
                    }
                });
            }
        });
    }

    private void redirectToHomePage() {
        ProcessingIndicatorPopup.center();

        PlaceRequest placeRequest = new PlaceRequest.Builder()
                .nameToken(NameTokens.getOnLoginDefaultPage())
                .build();
        placeManager.revealPlace(placeRequest);
    }

}
