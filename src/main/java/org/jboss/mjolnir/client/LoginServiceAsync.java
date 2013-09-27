package org.jboss.mjolnir.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.mjolnir.authentication.GithubOrganization;
import org.jboss.mjolnir.authentication.KerberosUser;

import java.util.Set;

public interface  LoginServiceAsync {

    void login(String krb5Name, String password, AsyncCallback<Boolean> async);

    void logout(AsyncCallback<Void> async);

    void getKerberosUser(String krb5Name, AsyncCallback<KerberosUser> async);

    void setSession(AsyncCallback<Void> async);

    void registerKerberosUser(String krb5Name, String githubName, AsyncCallback<KerberosUser> async);

    void getAvailableOrganizations(AsyncCallback<Set<GithubOrganization>> async);

    void unsubscribe(String orgName, int teamId, String githubName, AsyncCallback<Void> async);

    void subscribe(String orgName, int teamId, String githubName, AsyncCallback<Void> async);

    void isRegistered(String krb5Name, AsyncCallback<Boolean> async);
}
