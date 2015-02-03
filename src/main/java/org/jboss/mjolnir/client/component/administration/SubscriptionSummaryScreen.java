package org.jboss.mjolnir.client.component.administration;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.client.rpc.XsrfTokenService;
import com.google.gwt.user.client.rpc.XsrfTokenServiceAsync;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import org.jboss.mjolnir.client.domain.Subscription;
import org.jboss.mjolnir.client.domain.SubscriptionSummary;
import org.jboss.mjolnir.client.service.AdministrationService;
import org.jboss.mjolnir.client.service.AdministrationServiceAsync;

import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class SubscriptionSummaryScreen extends Composite {

    private static final Logger logger = Logger.getLogger("");

    private AdministrationServiceAsync administrationService = AdministrationService.Util.getInstance();

    private HTMLPanel panel = new HTMLPanel("");

    public SubscriptionSummaryScreen() {
        initWidget(panel);

        final XsrfTokenServiceAsync xsrf = GWT.create(XsrfTokenService.class);
        ((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "xsrf");
        xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
            @Override
            public void onFailure(Throwable caught) {
                logger.log(Level.SEVERE, "Cant get XSRF token.", caught);
            }

            @Override
            public void onSuccess(XsrfToken result) {
                ((HasRpcToken) administrationService).setRpcToken(result);
                administrationService.getSubscriptionsSummary(new AsyncCallback<List<SubscriptionSummary>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        logger.log(Level.SEVERE, caught.getMessage(), caught);
                    }

                    @Override
                    public void onSuccess(List<SubscriptionSummary> result) {
                        for (SubscriptionSummary summary : result) {
                            createSubscriptionTable(summary);
                        }
                    }
                });
            }
        });
    }

    private void createSubscriptionTable(SubscriptionSummary subscriptionSummary) {
        final HTMLPanel headingPanel = new HTMLPanel("h3", subscriptionSummary.getOrganization().getName());
        panel.add(headingPanel);

        final CellTable<Subscription> subscriptionTable = new CellTable<Subscription>();
        panel.add(subscriptionTable);


        // column definitions

        final TextColumn<Subscription> krbNameCol = new TextColumn<Subscription>() {
            @Override
            public String getValue(Subscription object) {
                return object.getKerberosName();
            }
        };
        krbNameCol.setSortable(true);
        krbNameCol.setDefaultSortAscending(true);
        subscriptionTable.addColumn(krbNameCol, "Kerberos Username");

        final TextColumn<Subscription> gitHubNameCol = new TextColumn<Subscription>() {
            @Override
            public String getValue(Subscription object) {
                return object.getGitHubName();
            }
        };
        gitHubNameCol.setSortable(true);
        subscriptionTable.addColumn(gitHubNameCol, "GitHub Username");

        final TextColumn<Subscription> krbAccCol = new TextColumn<Subscription>() {
            @Override
            public String getValue(Subscription object) {
                return object.isActiveKerberosAccount() ? "yes" : "no";
            }
        };
        krbAccCol.setSortable(true);
        subscriptionTable.addColumn(krbAccCol, "Active Krb Account?");


        // sorting

        final ColumnSortEvent.ListHandler<Subscription> sortHandler =
                new ColumnSortEvent.ListHandler<Subscription>(subscriptionSummary.getSubscriptions());
        sortHandler.setComparator(krbNameCol, new Comparator<Subscription>() {
            @Override
            public int compare(Subscription subscription, Subscription subscription2) {
                if (subscription == null || subscription.getKerberosName() == null) {
                    return -1;
                } else if (subscription2 == null || subscription2.getKerberosName() == null) {
                    return 1;
                }
                return subscription.getKerberosName().compareTo(subscription2.getKerberosName());
            }
        });
        sortHandler.setComparator(gitHubNameCol, new Comparator<Subscription>() {
            @Override
            public int compare(Subscription subscription, Subscription subscription2) {
                if (subscription == null || subscription.getGitHubName() == null) {
                    return -1;
                } else if (subscription2 == null || subscription2.getGitHubName() == null) {
                    return 1;
                }
                return subscription.getGitHubName().compareTo(subscription2.getGitHubName());
            }
        });
        sortHandler.setComparator(krbAccCol, new Comparator<Subscription>() {
            @Override
            public int compare(Subscription subscription, Subscription subscription2) {
                if (subscription == null) {
                    return -1;
                } else if (subscription2 == null) {
                    return 1;
                }
                return Boolean.valueOf(subscription.isActiveKerberosAccount())
                        .compareTo(Boolean.valueOf(subscription2.getKerberosName()));
            }
        });
        subscriptionTable.addColumnSortHandler(sortHandler);


        // setting data

        subscriptionTable.setRowCount(subscriptionSummary.getSubscriptions().size());
        subscriptionTable.setRowData(0, subscriptionSummary.getSubscriptions());
    }
}
