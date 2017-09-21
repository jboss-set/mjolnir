package org.jboss.set.mjolnir.client.exception;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * General application exception that can be propagated to client.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
@javax.ejb.ApplicationException(rollback = true)
public class ApplicationException extends RuntimeException implements IsSerializable {
    public ApplicationException() {
    }

    public ApplicationException(String message) {
        super(message);
    }

    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApplicationException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
