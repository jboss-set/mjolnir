package org.jboss.set.mjolnir.client.application.admin.gitHubMembers;

import java.util.Collections;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.jboss.set.mjolnir.client.component.ConfirmationDialog;
import org.jboss.set.mjolnir.client.component.administration.SubscriptionsTable;
import org.jboss.set.mjolnir.client.component.organizations.SelectionTable;
import org.jboss.set.mjolnir.shared.domain.Subscription;
import org.jboss.set.mjolnir.shared.domain.SubscriptionSummary;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class GitHubMembersView extends ViewWithUiHandlers<GitHubMembersHandlers>
        implements GitHubMembersPresenter.MyView {

    interface Templates extends SafeHtmlTemplates {
        @Template("Unsubscribe {0} users from GitHub organizations?")
        SafeHtml unsubscribeUsers(int number);
    }
    private static final Templates TEMPLATES = GWT.create(Templates.class);

    private SelectionTable<SubscriptionSummary> organizationsTable;
    private SubscriptionsTable subscriptionsTable;

    @Inject
    public GitHubMembersView() {
        FlowPanel panel = new FlowPanel();
        initWidget(panel);

        panel.add(new HTMLPanel("h2", "GitHub Organization Members"));

        panel.add(new HTMLPanel("h3", "Organizations"));
        panel.add(organizationsTable = createOrganizationsTable());

        panel.add(new HTMLPanel("h3", "Organization Members"));
        panel.add(subscriptionsTable = createSubscriptionTable());
    }

    private SelectionTable<SubscriptionSummary> createOrganizationsTable() {
        return new SelectionTable<SubscriptionSummary>() {
            @Override
            protected Object getKey(SubscriptionSummary item) {
                return item != null && item.getOrganization() != null ? item.getOrganization().getName() : null;
            }

            @Override
            protected String getName(SubscriptionSummary item) {
                return item != null && item.getOrganization() != null ? item.getOrganization().getName() : null;
            }

            @Override
            protected void onSelectionChanged(SubscriptionSummary selectedObject) {
                subscriptionsTable.setData(selectedObject != null ? selectedObject.getSubscriptions() : Collections.<Subscription>emptyList());
            }
        };
    }

    private SubscriptionsTable createSubscriptionTable() {
        SubscriptionsTable table = new SubscriptionsTable() {
            @Override
            protected void dispatchWhitelist(List<Subscription> selectedItems, boolean whitelist) {
                getUiHandlers().whitelist(selectedItems, whitelist);
            }
        };
        table.addAction("Unsubscribe", new UnsubscribeActionClickHandler(), true, false);
        return table;
    }


    private class UnsubscribeActionClickHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            final List<Subscription> selectedItems = subscriptionsTable.getSelectedItems();
            final ConfirmationDialog confirmDialog =
                    new ConfirmationDialog(TEMPLATES.unsubscribeUsers(selectedItems.size()).asString()) {
                        @Override
                        public void onConfirm() {
                            getUiHandlers().unsubscribeUsers(selectedItems);
                        }
                    };
            confirmDialog.center();
        }
    }

    @Override
    public void setData(List<SubscriptionSummary> items) {
        organizationsTable.setData(items);
    }

    @Override
    public List<Subscription> getCurrentSubscriptionList() {
        return subscriptionsTable.getItemList();
    }

    @Override
    public void refresh() {
        subscriptionsTable.refresh();
    }
}
