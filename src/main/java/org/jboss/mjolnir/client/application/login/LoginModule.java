package org.jboss.mjolnir.client.application.login;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class LoginModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(LoginPresenter.class, LoginPresenter.MyView.class, LoginView.class, LoginPresenter.MyProxy.class);
    }
}
