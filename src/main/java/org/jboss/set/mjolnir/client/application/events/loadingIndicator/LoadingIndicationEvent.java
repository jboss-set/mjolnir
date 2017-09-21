package org.jboss.set.mjolnir.client.application.events.loadingIndicator;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class LoadingIndicationEvent extends GwtEvent<LoadingIndicationEvent.LoadingIndicatorHandler> {

    public interface LoadingIndicatorHandler extends EventHandler {
        void onLoadingEvent(LoadingIndicationEvent event);
    }

    private boolean start;

    public static final Type<LoadingIndicatorHandler> TYPE = new Type<>();

    public LoadingIndicationEvent(boolean start) {
        this.start = start;
    }

    public static void fire(HasHandlers source, boolean start) {
        source.fireEvent(new LoadingIndicationEvent(start));
    }

    @Override
    public Type<LoadingIndicatorHandler> getAssociatedType() {
        return TYPE;
    }

    // this fixes GWT compilation error
    public static Type<LoadingIndicatorHandler> getType() {
        return TYPE;
    }

    @Override
    protected void dispatch(LoadingIndicatorHandler handler) {
        handler.onLoadingEvent(this);
    }

    public boolean isStart() {
        return start;
    }
}
