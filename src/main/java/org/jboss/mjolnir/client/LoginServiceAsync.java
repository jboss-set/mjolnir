package org.jboss.mjolnir.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.jboss.mjolnir.authentication.KerberosUser;

import javax.security.auth.login.LoginException;
import java.net.URISyntaxException;

public interface LoginServiceAsync {

    void login(String krb5Name, String githubName, String password, AsyncCallback<KerberosUser> async);

    void loginFromSession(AsyncCallback<KerberosUser> async);

    void logout(AsyncCallback<Void> async);

    void setSession(AsyncCallback<Void> async);
}
