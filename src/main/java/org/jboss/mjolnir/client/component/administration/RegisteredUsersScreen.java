package org.jboss.mjolnir.client.component.administration;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import org.jboss.mjolnir.client.ExceptionHandler;
import org.jboss.mjolnir.client.XsrfUtil;
import org.jboss.mjolnir.client.domain.Subscription;
import org.jboss.mjolnir.client.service.AdministrationService;
import org.jboss.mjolnir.client.service.AdministrationServiceAsync;

import java.util.List;

/**
 * Screen showing list of users registered in Mjolnir.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class RegisteredUsersScreen extends Composite {

    private AdministrationServiceAsync administrationService = AdministrationService.Util.getInstance();

    private HTMLPanel panel = new HTMLPanel("");

    public RegisteredUsersScreen() {
        initWidget(panel);

        panel.add(new HTMLPanel("h2", "Users Registered in Mjolnir"));

        XsrfUtil.obtainToken(new XsrfUtil.Callback() {
            @Override
            public void onSuccess(XsrfToken token) {
                ((HasRpcToken) administrationService).setRpcToken(token);
                administrationService.getRegisteredUsers(new AsyncCallback<List<Subscription>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        ExceptionHandler.handle("Couldn't get registered users.", caught);
                    }

                    @Override
                    public void onSuccess(List<Subscription> result) {
                        panel.add(new SubscriptionsTable(result));
                    }
                });
            }
        });
    }
}
