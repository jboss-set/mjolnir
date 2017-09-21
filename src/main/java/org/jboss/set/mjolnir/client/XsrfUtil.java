package org.jboss.set.mjolnir.client;

import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.client.rpc.XsrfTokenService;
import com.google.gwt.user.client.rpc.XsrfTokenServiceAsync;

/**
 * Utility class for XSRF protection setup.
 *
 * XsrfUtil.obtainToken() token must be called first, e.g. in your initial presenter's
 * onReveal() method. After that, XSRF token can be assigned to services via
 * "XsrfUtil.putToken(service)".
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class XsrfUtil {

    private static Logger log = Logger.getGlobal();
    private static XsrfToken token;

    public static void obtainToken(final Callback callback) {
        log.info("Obtaining token.");
        final XsrfTokenServiceAsync xsrf = GWT.create(XsrfTokenService.class);
        ((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "xsrf");
        xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
            @Override
            public void onFailure(Throwable caught) {
                log.warning("Error while obtaining token.");
                ExceptionHandler.handle("Cant get XSRF token", caught);
            }

            @Override
            public void onSuccess(XsrfToken result) {
                log.info("Received token.");
                token = result;
                if (callback != null) {
                    callback.onSuccess(token);
                }
            }
        });
    }

    public interface Callback {
        void onSuccess(XsrfToken token);
    }

    public static void putToken(HasRpcToken service) {
        if (token == null) {
            log.severe("Token not initialized. XsrfUtil.obtainToken() must be called before putToken(), e.g. in your main Module class.");
        }
        service.setRpcToken(token);
    }

    public static void setToken(XsrfToken token) {
        assert token != null;
        XsrfUtil.token = token;
    }
}
