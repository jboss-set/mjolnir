package org.jboss.mjolnir.client.component.administration;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.client.rpc.XsrfTokenService;
import com.google.gwt.user.client.rpc.XsrfTokenServiceAsync;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import org.jboss.mjolnir.client.ExceptionHandler;
import org.jboss.mjolnir.client.component.LoadingPanel;
import org.jboss.mjolnir.client.domain.Subscription;
import org.jboss.mjolnir.client.domain.SubscriptionSummary;
import org.jboss.mjolnir.client.service.AdministrationService;
import org.jboss.mjolnir.client.service.AdministrationServiceAsync;

import java.util.List;

/**
 * Screen showing list of users subscribed in GitHub organization.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class SubscriptionSummaryScreen extends Composite {

    private AdministrationServiceAsync administrationService = AdministrationService.Util.getInstance();

    private HTMLPanel panel = new HTMLPanel("");
    private LoadingPanel loadingPanel = new LoadingPanel();

    public SubscriptionSummaryScreen() {
        initWidget(panel);

        panel.add(new HTMLPanel("h2", "GitHub Organization Members"));
        panel.add(loadingPanel);

        final XsrfTokenServiceAsync xsrf = GWT.create(XsrfTokenService.class);
        ((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "xsrf");
        xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
            @Override
            public void onFailure(Throwable caught) {
                ExceptionHandler.handle("Cant get XSRF token.", caught);
            }

            @Override
            public void onSuccess(XsrfToken result) {
                ((HasRpcToken) administrationService).setRpcToken(result);
                administrationService.getOrganizationMembers(new AsyncCallback<List<SubscriptionSummary>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        ExceptionHandler.handle(caught);
                    }

                    @Override
                    public void onSuccess(List<SubscriptionSummary> result) {
                        loadingPanel.removeFromParent();
                        for (SubscriptionSummary summary : result) {
                            createSubscriptionTable(summary);
                        }
                    }
                });
            }
        });
    }

    private void createSubscriptionTable(SubscriptionSummary subscriptionSummary) {
        panel.add(new HTMLPanel("h3", subscriptionSummary.getOrganization().getName()));
        panel.add(new SubscriptionsTable(subscriptionSummary.getSubscriptions()) {
            @Override
            protected void onDeleted(Subscription object) {
                // remove KerberosUser instance, but Subscription instance must remain in the list,
                // since GitHub subscriptions were not removed
                object.setKerberosUser(null);
                dataProvider.refresh();
            }
        });
    }
}
