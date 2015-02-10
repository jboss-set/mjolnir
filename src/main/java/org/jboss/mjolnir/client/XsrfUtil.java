package org.jboss.mjolnir.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.client.rpc.XsrfTokenService;
import com.google.gwt.user.client.rpc.XsrfTokenServiceAsync;

import java.util.logging.Logger;

/**
 * Utility class for easier XSRF token obtaining.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class XsrfUtil {

    private static final Logger logger = Logger.getLogger("");

    public static void obtainToken(final Callback callback) {
        final XsrfTokenServiceAsync xsrf = GWT.create(XsrfTokenService.class);
        ((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "xsrf");
        xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
            @Override
            public void onFailure(Throwable caught) {
                ExceptionHandler.handle("Cant get XSRF token." ,caught);
            }

            @Override
            public void onSuccess(XsrfToken token) {
                callback.onSuccess(token);
            }
        });
    }

    public static interface Callback {
        void onSuccess(XsrfToken token);
    }
}
