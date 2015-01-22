package org.jboss.mjolnir.client.exception;

/**
 * General application exception that can be propagated to client.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class ApplicationException extends RuntimeException {
    public ApplicationException() {
    }

    public ApplicationException(String message) {
        super(message);
    }

    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApplicationException(Throwable cause) {
        super(cause);
    }
}
