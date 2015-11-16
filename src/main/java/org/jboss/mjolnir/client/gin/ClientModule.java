package org.jboss.mjolnir.client.gin;

import com.google.inject.Singleton;
import com.gwtplatform.mvp.client.annotations.DefaultPlace;
import com.gwtplatform.mvp.client.annotations.ErrorPlace;
import com.gwtplatform.mvp.client.annotations.UnauthorizedPlace;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.gwtplatform.mvp.client.gin.DefaultModule;
import org.jboss.mjolnir.client.NameTokens;
import org.jboss.mjolnir.client.application.ApplicationModule;
import org.jboss.mjolnir.client.application.login.LoginModule;
import org.jboss.mjolnir.client.application.security.CurrentUser2;
import org.jboss.mjolnir.client.application.security.IsAdminGatekeeper;
import org.jboss.mjolnir.client.application.security.LoggedInGatekeeper;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class ClientModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        install(new DefaultModule.Builder().build());

        install(new ApplicationModule());
        install(new LoginModule());

        // DefaultPlaceManager Places
        bindConstant().annotatedWith(DefaultPlace.class).to(NameTokens.MY_SUBSCRIPTIONS);
        bindConstant().annotatedWith(ErrorPlace.class).to(NameTokens.ERROR);
        bindConstant().annotatedWith(UnauthorizedPlace.class).to(NameTokens.LOGIN);

        bind(CurrentUser2.class).in(Singleton.class);
        bind(LoggedInGatekeeper.class).in(Singleton.class);
        bind(IsAdminGatekeeper.class).in(Singleton.class);
    }
}
