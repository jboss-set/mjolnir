package org.jboss.mjolnir.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.mjolnir.client.ExceptionHandler;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public abstract class DefaultCallback<T> implements AsyncCallback<T> {
    @Override
    public void onFailure(Throwable caught) {
        ExceptionHandler.handle(caught);
    }
}
