package org.jboss.mjolnir.client.component;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class LoadingPanel extends Composite {

    public LoadingPanel() {
        initWidget(new HTMLPanel("<center><div id='loading-panel'><img src='images/loading.gif' style='padding-top:3px;vertical-align:middle'/> Loading... </div></center>"));
        getWidget().getElement().getStyle().setPaddingTop(5, Style.Unit.EM);

    }
}
