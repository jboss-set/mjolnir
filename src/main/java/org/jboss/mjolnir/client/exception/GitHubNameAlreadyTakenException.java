package org.jboss.mjolnir.client.exception;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Exception indicating that user can't set his GitHub name to given value, because it is already being used
 * by different user.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class GitHubNameAlreadyTakenException extends Exception implements IsSerializable {

    public GitHubNameAlreadyTakenException() {
    }

    public GitHubNameAlreadyTakenException(String message) {
        super(message);
    }

}
