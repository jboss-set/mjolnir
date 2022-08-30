package org.jboss.set.mjolnir.client.application.events.loadingIndicator;

import java.util.logging.Logger;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class LoadingIndicationEvent extends GwtEvent<LoadingIndicationEvent.LoadingIndicatorHandler> {

    private static final Logger logger = Logger.getLogger(LoadingIndicationEvent.class.getName());

    public interface LoadingIndicatorHandler extends EventHandler {
        void onLoadingEvent(LoadingIndicationEvent event);
    }

    private final boolean start;

    public static final Type<LoadingIndicatorHandler> TYPE = new Type<>();

    public LoadingIndicationEvent(boolean start) {
        this.start = start;
    }

    public static void show(HasHandlers source, String message) {
        logger.info(message);
        source.fireEvent(new LoadingIndicationEvent(true));
    }

    public static void hide(HasHandlers source) {
        source.fireEvent(new LoadingIndicationEvent(false));
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
