/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.mjolnir.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.jboss.mjolnir.authentication.GithubOrganization;
import org.jboss.mjolnir.authentication.GithubTeam;
import org.jboss.mjolnir.authentication.KerberosUser;

import java.util.List;
import java.util.Set;

/**
 * @author: navssurtani
 * @since: 0.2
 */

public class SubscriptionScreen extends Composite {

    private KerberosUser user;

    private LoginServiceAsync loginService;
    private RootPanel successPanel = RootPanel.get("subscriptionPanelContainer");

    public SubscriptionScreen(KerberosUser user) {
        this.user = user;
        loginService = LoginService.Util.getInstance();

        String introductionString = "Organizations and teams subscribed to for " + user.getGithubName();
        successPanel.add(new Label(introductionString));
        loadOrgsFromSubscriptionService();
    }

    public void loadOrgsFromSubscriptionService() {
        loginService.getAvailableOrganizations(new AsyncCallback<Set<GithubOrganization>>() {
            @Override
            public void onFailure(Throwable caught) {
                displayPopupBox("Could not get available organizations", caught.getMessage());
            }

            @Override
            public void onSuccess(Set<GithubOrganization> result) {
                successPanel.add(new Label("Result size from server is " + result.size()));
                generateGrids(result);
            }
        });
    }

    private void generateGrids(Set<GithubOrganization> result) {
        // For each organization we will create a grid and then populate it.
        // We know that universally, these grids should have 4 columns.
        int gridCols = 4;

        for (GithubOrganization o : result) {
            // For each organization, we want the number of teams + 2.
            List<GithubTeam> teams = o.getTeams();
            int teamSize = teams.size();
            int gridRows = teamSize + 2;

            Grid orgGrid = new Grid(gridRows, gridCols);
            populateBasicContent(orgGrid, o.getName());

            for (int i = 0; i < teamSize; i++) {
                GithubTeam team = teams.get(i);
                // By default here our row index will be (i+2).
                int rowIndex = i + 2;
                orgGrid.setWidget(rowIndex, 0, new Label(team.getName()));
                orgGrid.setWidget(rowIndex, 1, generateSubscribeButton(o.getName(), team.getId()));
                orgGrid.setWidget(rowIndex, 2, generateUnsubscribeButton(o.getName(), team.getId()));

                // TODO: Find a way to generate the live status of the user here.
            }
            successPanel.add(orgGrid);
        }
    }

    private void populateBasicContent(Grid orgGrid, String orgName) {
        // The first row and first column is static.
        orgGrid.setWidget(0, 0, new Label("Organization: "));
        // The second column will have the organization name.
        orgGrid.setWidget(0, 1, new Label(orgName));
        // Second row and first column is just static again.
        orgGrid.setWidget(1, 0, new Label("Teams: "));

    }

    private Button generateSubscribeButton(final String orgName, final int teamId) {
        Button subscribeButton = crreateOperationButton("Subscribe");
        subscribeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                loginService.subscribe(orgName, teamId, user.getGithubName(), getCallBack("subscirbe", orgName, teamId));
            }
        });
        return subscribeButton;
    }

    private Button generateUnsubscribeButton(final String orgName, final int teamId) {
        Button unsubscribeButton = crreateOperationButton("Unsubscribe");
        unsubscribeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                loginService.unsubscribe(orgName, teamId, user.getGithubName(), getCallBack("unsubscribe",
                        orgName, teamId));
            }
        });

        return unsubscribeButton;
    }

    private AsyncCallback<Void> getCallBack(final String operationName, final String orgName, final int teamId) {
        AsyncCallback<Void> toReturn = new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                displayPopupBox("Error with " + operationName + " operation", caught.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
                displayPopupBox("Successful operation",
                        "Successful attempt to " + operationName + " to team #" + teamId + " for organization " + orgName);
            }
        };
        return toReturn;
    }

    private Button crreateOperationButton(String buttonName) {
        Button b = new Button(buttonName);
        b.setEnabled(true);
        b.getElement().setId(buttonName);
        return b;
    }

    private void displayPopupBox(String header, String message) {
        final DialogBox box = new DialogBox();
        box.setText(header);
        final HTML html = new HTML();
        html.setHTML(message);
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        final Button closeButton = buildCloseButton(box);
        verticalPanel.add(html);
        verticalPanel.add(closeButton);
        box.setWidget(verticalPanel);
        box.center();
    }

    private Button buildCloseButton(final DialogBox box) {
        final Button closeButton = new Button("Close");
        closeButton.setEnabled(true);
        closeButton.getElement().setId("Close");
        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                box.hide();
            }
        });
        return closeButton;
    }

}
