package org.jboss.mjolnir.client;

import org.jboss.mjolnir.client.component.AuthenticationTimedOutDialog;
import org.jboss.mjolnir.client.component.ErrorDialog;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.StatusCodeException;

/**
 * Utility class for handling exceptions.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class ExceptionHandler {

    private static final Logger logger = Logger.getLogger("");

    private ExceptionHandler() {
    }

    public static void handle(Throwable throwable) {
        handle(null, throwable);
    }


    public static void handle(String message, Throwable throwable) {
        if (throwable instanceof StatusCodeException) {
            StatusCodeException ex = (StatusCodeException) throwable;
            if (ex.getStatusCode() == 401) {
                AuthenticationTimedOutDialog dialog = new AuthenticationTimedOutDialog();
                dialog.center();
                return;
            }
        }
        logger.log(Level.SEVERE, message, throwable);

        final ErrorDialog errorDialog = new ErrorDialog(message, throwable);
        errorDialog.center();
    }
}
