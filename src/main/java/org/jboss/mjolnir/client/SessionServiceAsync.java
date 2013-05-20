package org.jboss.mjolnir.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;

public interface SessionServiceAsync {
    void createSession(AsyncCallback<Void> async);
}
