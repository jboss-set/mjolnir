package org.jboss.set.mjolnir.client.component;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * Panel showing rotating picture to indicate that some processing is being performed.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class LoadingPanel extends Composite {

    public LoadingPanel() {
        initWidget(new HTMLPanel("<center><div id='loading-panel'><img src='images/loading.gif' style='padding-top:3px;vertical-align:middle'/></div></center>"));
    }
}
