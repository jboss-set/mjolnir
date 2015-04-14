package org.jboss.mjolnir.client.component.administration;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.HasCell;
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
import org.jboss.mjolnir.client.component.ConfirmationDialog;
import org.jboss.mjolnir.client.component.LoadingPanel;
import org.jboss.mjolnir.client.component.table.ConditionalActionCell;
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

    private static final String UNSUBSCRIBE_USER_TEXT = "Remove user from GitHub organizations?";

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
            protected List<HasCell<Subscription, ?>> createActionCells() {
                List<HasCell<Subscription, ?>> actionCells = super.createActionCells();
                actionCells.add(2, new ConditionalActionCell<Subscription>("Unsubscribe", new UnsubscribeDelegate()) {
                    @Override
                    public boolean isEnabled(Subscription value) {
                        return value.getGitHubName() != null;
                    }
                });
                return actionCells;
            }

            @Override
            protected void onDeleted(Subscription object) {
                // remove KerberosUser instance, but Subscription instance must remain in the list,
                // since GitHub subscriptions were not removed
                object.setKerberosUser(null);
                dataProvider.refresh();
            }
        });
    }


    private class UnsubscribeDelegate implements ActionCell.Delegate<Subscription> {
        @Override
        public void execute(final Subscription object) {
            final ConfirmationDialog confirmDialog = new ConfirmationDialog(UNSUBSCRIBE_USER_TEXT) {
                @Override
                public void onConfirm() {
                    administrationService.unsubscribe(object.getGitHubName(), new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            ExceptionHandler.handle(caught);
                        }

                        @Override
                        public void onSuccess(Void result) {
                        }
                    });
                }
            };
            confirmDialog.center();
        }
    }

}
