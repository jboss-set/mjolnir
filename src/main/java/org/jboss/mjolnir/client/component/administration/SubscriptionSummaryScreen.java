package org.jboss.mjolnir.client.component.administration;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.client.rpc.XsrfTokenService;
import com.google.gwt.user.client.rpc.XsrfTokenServiceAsync;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import org.jboss.mjolnir.client.ExceptionHandler;
import org.jboss.mjolnir.client.component.ConfirmationDialog;
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

    interface Templates extends SafeHtmlTemplates {
        @Template("Unsubscribe {0} users from GitHub organizations?")
        SafeHtml unsubscribeUsers(int number);
    }
    private static final Templates TEMPLATES = GWT.create(Templates.class);


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
        SubscriptionsTable table = new SubscriptionsTable(subscriptionSummary.getSubscriptions());
        table.addAction("Unsubscribe", new UnsubscribeActionDelegate(table), true, false);

        panel.add(new HTMLPanel("h3", subscriptionSummary.getOrganization().getName()));
        panel.add(table);
    }


    private class UnsubscribeActionDelegate implements SubscriptionsTable.ActionDelegate {

        private SubscriptionsTable table;

        public UnsubscribeActionDelegate(SubscriptionsTable table) {
            this.table = table;
        }

        @Override
        public void execute(final List<Subscription> selectedItems) {
            final ConfirmationDialog confirmDialog =
                    new ConfirmationDialog(TEMPLATES.unsubscribeUsers(selectedItems.size()).asString()) {
                @Override
                public void onConfirm() {
                    administrationService.unsubscribe(selectedItems, new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            ExceptionHandler.handle(caught);
                        }

                        @Override
                        public void onSuccess(Void result) {
                            table.getDataProvider().getList().removeAll(selectedItems);
                        }
                    });
                }
            };
            confirmDialog.center();
        }
    }
}
