package org.jboss.set.mjolnir.client.component;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * Popup indicator showing animation suggesting that some processing is under way
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class ProcessingIndicatorPopup {

    private static PopupPanel instance;

    static {
        instance = new PopupPanel(false, true);
        instance.setWidget(new LoadingPanel());
        instance.setGlassEnabled(true);
        instance.getElement().getStyle().setBorderWidth(0, Style.Unit.PX);
    }

    private ProcessingIndicatorPopup() {
    }

    public static void center() {
        instance.center();
    }

    public static void hide() {
        instance.hide();
    }

}
