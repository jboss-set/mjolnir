package org.jboss.mjolnir.client.exception;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Exception indicating that given report data are no longer available in session.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class ReportDataNotAvailableException extends Exception implements IsSerializable {

    public ReportDataNotAvailableException() {
    }

    public ReportDataNotAvailableException(String message) {
        super(message);
    }

}
