package org.jboss.set.mjolnir.client.application.login;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public interface LoginHandlers extends UiHandlers {

    void login(String username, String password);

}
