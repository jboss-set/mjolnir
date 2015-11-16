package org.jboss.mjolnir.client.application.subscriptionSetting;

import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.jboss.mjolnir.client.component.organizations.SelectionTable;
import org.jboss.mjolnir.shared.domain.GithubOrganization;
import org.jboss.mjolnir.shared.domain.GithubTeam;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class SubscriptionSettingView extends ViewWithUiHandlers<SubscribtionHandlers>
        implements SubscriptionSettingPresenter.MyView {

    private MySubscriptionsWidget mySubscriptionsWidget;
    private SelectionTable<GithubOrganization> organizationsTable;

    @Inject
    public SubscriptionSettingView() {
        FlowPanel panel = new FlowPanel();
        initWidget(panel);

        panel.add(new HTMLPanel("h2", "Your GitHub Subscriptions"));

        panel.add(new HTMLPanel("h3", "Organizations"));
        panel.add(organizationsTable = new SelectionTable<GithubOrganization>() {
            @Override
            protected Object getKey(GithubOrganization item) {
                return item != null ? item.getName() : null;
            }

            @Override
            protected String getName(GithubOrganization item) {
                return item != null ? item.getName() : "";
            }

            @Override
            protected void onSelectionChanged(GithubOrganization selectedObject) {
                mySubscriptionsWidget.setData(selectedObject != null ? selectedObject.getTeams() : Collections.<GithubTeam>emptyList());
            }
        });

        panel.add(new HTMLPanel("h3", "Teams"));
        panel.add(mySubscriptionsWidget = new MySubscriptionsWidget() {
            @Override
            protected void dispatchSubscribe(GithubTeam team) {
                getUiHandlers().subscribe(team);
            }

            @Override
            protected void dispatchUnSubscribe(GithubTeam team) {
                getUiHandlers().unsubscribe(team);
            }
        });
    }

    @Override
    public void setData(List<GithubOrganization> organizations) {
        organizationsTable.setData(organizations);
    }

    @Override
    public void refresh() {
        mySubscriptionsWidget.refresh();
    }
}
