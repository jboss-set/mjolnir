package org.jboss.mjolnir.client;

import org.jboss.mjolnir.client.component.ErrorDialog;

import java.util.logging.Level;
import java.util.logging.Logger;

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
        logger.log(Level.SEVERE, message, throwable);

        final ErrorDialog errorDialog = new ErrorDialog(message, throwable);
        errorDialog.center();
    }
}
