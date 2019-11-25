package org.jboss.set.mjolnir.client.application.admin.gitHubMembers;

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
import org.jboss.set.mjolnir.shared.domain.GithubOrganization;
import org.jboss.set.mjolnir.shared.domain.GithubTeam;
import org.jboss.set.mjolnir.shared.domain.Subscription;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    private SelectionTable<GithubOrganization> organizationsTable;
    private SelectionTable<GithubTeam> teamsTable;
    private SubscriptionsTable subscriptionsTable;
    private GithubOrganization selectedOrg;

    @Inject
    public GitHubMembersView() {
        FlowPanel panel = new FlowPanel();
        initWidget(panel);

        panel.add(new HTMLPanel("h2", "GitHub Organization Members"));

        panel.add(new HTMLPanel("h3", "Organizations"));
        panel.add(organizationsTable = createOrganizationsTable());

        panel.add(new HTMLPanel("h3", "Teams"));
        panel.add(teamsTable = createTeamsTable());

        panel.add(new HTMLPanel("h3", "Organization Members"));
        panel.add(subscriptionsTable = createSubscriptionTable());
    }

    private SelectionTable<GithubOrganization> createOrganizationsTable() {
        return new SelectionTable<GithubOrganization>() {
            @Override
            protected Object getKey(GithubOrganization item) {
                return item != null ? item.getName() : null;
            }

            @Override
            protected String getName(GithubOrganization item) {
                return item != null ? item.getName() : null;
            }

            @Override
            protected void onSelectionChanged(GithubOrganization selectedObject) {
                selectedOrg = selectedObject;
                subscriptionsTable.setData(Collections.<Subscription>emptyList());
                teamsTable.setData(selectedObject != null ?
                        selectedObject.getTeams() : Collections.<GithubTeam>emptyList());
            }
        };
    }

    private SelectionTable<GithubTeam> createTeamsTable() {
        return new SelectionTable<GithubTeam>() {
            @Override
            protected Object getKey(GithubTeam item) {
                return item != null ? item.getId() : null;
            }

            @Override
            protected String getName(GithubTeam item) {
                return item != null ? item.getName() : null;
            }

            @Override
            protected void onSelectionChanged(GithubTeam selectedObject) {
                subscriptionsTable.setData(Collections.<Subscription>emptyList());
                if (selectedOrg != null) {
                    getUiHandlers().retrieveSubscriptions(selectedOrg, selectedObject);
                }
            }

            @Override
            public void setData(List<GithubTeam> values) {
                // prepend "All teams" option
                values = new ArrayList<>(values);
                GithubTeam allTeamsItem = new GithubTeam("All teams", null);
                values.add(0, allTeamsItem);
                super.setData(values);
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
    public void setOrganizations(List<GithubOrganization> items) {
        organizationsTable.setData(items);
    }

    @Override
    public void setSubscriptions(List<Subscription> items) {
        subscriptionsTable.setData(items);
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
