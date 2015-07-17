package org.jboss.mjolnir.client.exception;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Exception indicating expected / recoverable condition.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class LogicalException extends Exception implements IsSerializable {

    public LogicalException(String message) {
        super(message);
    }

}
