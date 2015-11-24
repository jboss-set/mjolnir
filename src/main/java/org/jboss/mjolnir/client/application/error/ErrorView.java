package org.jboss.mjolnir.client.application.error;

import com.google.inject.Inject;
import com.google.gwt.user.client.ui.Label;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class ErrorView extends ViewImpl implements ErrorPresenter.MyView {

    @Inject
    public ErrorView() {
        initWidget(new Label("Error"));
    }
}
