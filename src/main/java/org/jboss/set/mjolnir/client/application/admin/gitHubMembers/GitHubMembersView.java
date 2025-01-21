package org.jboss.set.mjolnir.client.application.admin.gitHubMembers;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
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
 * Displays table of members of selected GH organization & team.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class GitHubMembersView extends ViewWithUiHandlers<GitHubMembersHandlers>
        implements GitHubMembersPresenter.MyView {

    interface Templates extends SafeHtmlTemplates {
        @Template("Unsubscribe {0} users from GitHub organizations?")
        SafeHtml unsubscribeUsers(int number);
    }
    private static final Templates TEMPLATES = GWT.create(Templates.class);

    private final SelectionTable<GithubOrganization> organizationsTable;
    private final SelectionTable<GithubTeam> teamsTable;
    private final SubscriptionsTable subscriptionsTable;
    private GithubOrganization selectedOrg;
    private GithubTeam selectedTeam;

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
        Anchor downloadLink = new Anchor("Download CSV", false);
        downloadLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                SubscriptionsTable.SubscriptionSearchPredicate sp = subscriptionsTable.getSearchPredicate();
                String url = GWT.getModuleBaseURL()
                        + "auth/download?org=" + selectedOrg.getName()
                        + "&team=" + (selectedTeam.getId() != null ? selectedTeam.getId() : "")
                        + "&krbName=" + sp.getKrbNameExpression()
                        + "&ghName=" + sp.getGitHubNameExpression()
                        + "&krbAccount=" + (sp.getKrbAccount() != null ? sp.getKrbAccount() : "")
                        + "&whitelisted=" + (sp.getWhitelisted() != null ? sp.getWhitelisted() : "");
                Window.open(url, "_blank", "");
            }
        });
        panel.add(downloadLink);
        panel.add(subscriptionsTable = createSubscriptionTable());
    }

    private SelectionTable<GithubOrganization> createOrganizationsTable() {
        return new SelectionTable<GithubOrganization>() {
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
            protected String getName(GithubTeam item) {
                return item != null ? item.getName() : null;
            }

            @Override
            protected void onSelectionChanged(GithubTeam selectedTeam) {
                GitHubMembersView.this.selectedTeam = selectedTeam;
                subscriptionsTable.setData(Collections.<Subscription>emptyList());
                if (selectedOrg != null) {
                    getUiHandlers().retrieveSubscriptions(selectedOrg, selectedTeam);
                }
            }

            @Override
            public void setData(List<GithubTeam> values) {
                // prepend "All teams" option
                values = new ArrayList<>(values);
                GithubTeam allTeamsItem = new GithubTeam("All teams", null, false);
                values.add(0, allTeamsItem);
                super.setData(values);
            }
        };
    }

    private SubscriptionsTable createSubscriptionTable() {
        SubscriptionsTable table = new SubscriptionsTable();
        table.addAction("Unsubscribe", new UnsubscribeActionClickHandler(), false);
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
