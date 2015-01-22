package org.jboss.mjolnir.client.component;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.client.rpc.XsrfTokenService;
import com.google.gwt.user.client.rpc.XsrfTokenServiceAsync;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.mjolnir.authentication.GithubOrganization;
import org.jboss.mjolnir.authentication.GithubTeam;
import org.jboss.mjolnir.client.GitHubService;
import org.jboss.mjolnir.client.GitHubServiceAsync;
import org.jboss.mjolnir.server.github.MembershipState;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class SubscriptionScreen extends Composite {

    private static Logger logger = Logger.getLogger("");

    private GitHubServiceAsync gitHubService = GitHubService.Util.getInstance();

    public SubscriptionScreen() {
        final VerticalPanel panel = new VerticalPanel();
        initWidget(panel);

        panel.add(new GitHubNamePanel());

        final XsrfTokenServiceAsync xsrf = GWT.create(XsrfTokenService.class);
        ((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "xsrf");
        xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
            @Override
            public void onFailure(Throwable caught) {
                logger.log(Level.SEVERE, caught.getMessage(), caught);
            }

            @Override
            public void onSuccess(XsrfToken result) {
                ((HasRpcToken) gitHubService).setRpcToken(result);
                gitHubService.getSubscriptions(new AsyncCallback<Set<GithubOrganization>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        logger.log(Level.SEVERE, "Can't load organizations.", caught);
                    }

                    @Override
                    public void onSuccess(Set<GithubOrganization> result) {
                        panel.add(createSubscriptionTable(result));
                    }
                });
            }
        });
    }

    private Widget createSubscriptionTable(Set<GithubOrganization> organizations) {
        final HTMLPanel panel = new HTMLPanel("");
        for (GithubOrganization organization : organizations) {
            panel.add(new HTMLPanel("h3", "Organization " + organization.getName()));

            final CellTable<GithubTeam> cellTable = new CellTable<GithubTeam>();
            final TextColumn<GithubTeam> nameColumn = new TextColumn<GithubTeam>() {
                @Override
                public String getValue(GithubTeam team) {
                    return team.getName();
                }
            };
            cellTable.addColumn(nameColumn, "Team");

            final TextColumn<GithubTeam> subscribedColumn = new TextColumn<GithubTeam>() {
                @Override
                public String getValue(GithubTeam object) {
                    if (MembershipState.NONE.equals(object.getMembershipState())) {
                        return "no";
                    } else if (MembershipState.ACTIVE.equals(object.getMembershipState())) {
                        return "yes";
                    } else if (MembershipState.PENDING.equals(object.getMembershipState())) {
                        return "pending";
                    }
                    return "?"; // unknown state
                }
            };
            cellTable.addColumn(subscribedColumn, "Membership");

            final ButtonCell cell = new ButtonCell();
            final Column<GithubTeam, String> actionColumn = new Column<GithubTeam, String>(cell) {
                @Override
                public String getValue(GithubTeam object) {
                    return MembershipState.NONE.equals(object.getMembershipState())
                            ? "Subscribe" : "Unsubscribe";
                }
            };
            actionColumn.setFieldUpdater(new FieldUpdater<GithubTeam, String>() {
                @Override
                public void update(int index, final GithubTeam object, String value) {
                    if (MembershipState.NONE.equals(object.getMembershipState())) {
                        gitHubService.subscribe(object.getId(), new AsyncCallback<String>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                logger.log(Level.SEVERE, caught.getMessage(), caught);
                            }

                            @Override
                            public void onSuccess(String state) {
                                object.setMembershipState(state);
                                cellTable.redraw();
                            }
                        });
                    } else {
                        gitHubService.unsubscribe(object.getId(), new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                logger.log(Level.SEVERE, caught.getMessage(), caught);
                            }

                            @Override
                            public void onSuccess(Void result) {
                                object.setMembershipState(MembershipState.NONE);
                                cellTable.redraw();
                            }
                        });
                    }
                }
            });
            cellTable.addColumn(actionColumn);

            cellTable.setRowCount(organization.getTeams().size());
            cellTable.setRowData(0, organization.getTeams());

            panel.add(cellTable);
        }
        return panel;
    }
}
