package org.jboss.mjolnir.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.mjolnir.shared.domain.KerberosUser;

public interface  LoginServiceAsync {

    void login(String krb5Name, String password, AsyncCallback<KerberosUser> async);

    void logout(AsyncCallback<Void> async);

    void getLoggedUser(AsyncCallback<KerberosUser> async);

}
