package org.jboss.set.mjolnir.client.application.subscriptionSetting;

import java.util.Collections;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.jboss.set.mjolnir.client.component.organizations.SelectionTable;
import org.jboss.set.mjolnir.shared.domain.GithubOrganization;
import org.jboss.set.mjolnir.shared.domain.GithubTeam;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class SubscriptionSettingView extends ViewWithUiHandlers<SubscribtionHandlers>
        implements SubscriptionSettingPresenter.MyView {

    private MySubscriptionsWidget mySubscriptionsWidget;
    private SelectionTable<GithubOrganization> organizationsTable;
    private Label gitHubNameLabel;
//    private ModifyGitHubNamePopup modifyGitHubNamePopup;

    @Inject
    public SubscriptionSettingView() {
        FlowPanel panel = new FlowPanel();
        initWidget(panel);

        panel.add(new HTMLPanel("h2", "Your GitHub Subscriptions"));

        FlexTable flexTable = new FlexTable();
        panel.add(flexTable);
        flexTable.setWidget(0, 0, new Label("GitHub Username:"));
        flexTable.setWidget(0, 1, gitHubNameLabel = new Label(""));
        flexTable.setWidget(0, 3, new Button("Edit", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getUiHandlers().onGitHubNameNotSet();
//                modifyGitHubNamePopup.enableCancelButton(true);
//                modifyGitHubNamePopup.center();
            }
        }));

        panel.add(new HTMLPanel("h3", "Organizations"));
        panel.add(organizationsTable = new SelectionTable<GithubOrganization>() {
            @Override
            protected String getName(GithubOrganization item) {
                return item != null ? item.getName() : "";
            }

            @Override
            protected void onSelectionChanged(GithubOrganization selectedObject) {
                mySubscriptionsWidget.setData(selectedObject != null ? selectedObject.getSelfServiceTeams() : Collections.<GithubTeam>emptyList());
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

        /*modifyGitHubNamePopup = new ModifyGitHubNamePopup() {
            @Override
            public void onSubmit(String newUsername) {
                getUiHandlers().modifyGitHubName(newUsername);
            }
        };*/
    }

    @Override
    public void setGitHubName(String username) {
        gitHubNameLabel.setText(username);
//        modifyGitHubNamePopup.setUsername(username);
    }

    @Override
    public void setData(List<GithubOrganization> organizations) {
        organizationsTable.setData(organizations);
    }

    @Override
    public void refresh() {
        mySubscriptionsWidget.refresh();
    }

    /*@Override
    public ModifyGitHubNamePopup getGitHubNamePopup() {
        return modifyGitHubNamePopup;
    }*/
}
