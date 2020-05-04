package org.jboss.set.mjolnir.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.set.mjolnir.shared.domain.RegisteredUser;

@Deprecated
public interface LoginServiceAsync {

    void login(String krb5Name, String password, AsyncCallback<RegisteredUser> async);

    void logout(AsyncCallback<Void> async);

    void getLoggedUser(AsyncCallback<RegisteredUser> async);

}
