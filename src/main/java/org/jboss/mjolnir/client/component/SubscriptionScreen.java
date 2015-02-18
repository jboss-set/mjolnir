package org.jboss.mjolnir.client.component;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
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
import org.jboss.mjolnir.authentication.KerberosUser;
import org.jboss.mjolnir.client.CurrentUser;
import org.jboss.mjolnir.client.ExceptionHandler;
import org.jboss.mjolnir.client.service.GitHubService;
import org.jboss.mjolnir.client.service.GitHubServiceAsync;
import org.jboss.mjolnir.server.github.MembershipStates;

import java.util.Set;

/**
 * Screen that allows user to (un)subscribe to GitHub teams.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class SubscriptionScreen extends Composite {

    private final static String GITHUB_NAME_DIALOG_MESSAGE = "Before you can manage your subscriptions, please specify your GitHub name.";

    private GitHubServiceAsync gitHubService = GitHubService.Util.getInstance();
    private VerticalPanel panel = new VerticalPanel();
    private LoadingPanel loadingPanel = new LoadingPanel();

    public SubscriptionScreen() {
        initWidget(panel);

        panel.add(new HTMLPanel("h2", "Subscribe to GitHub Teams"));
        panel.add(loadingPanel);
        checkGitHubNameAndCreateContent();
    }

    private void checkGitHubNameAndCreateContent() {
        final String githubName = CurrentUser.get().getGithubName();
        if (githubName == null || "".equals(githubName)) {
            // if github name is not set, show popup
            final ModifyGitHubNamePopup popup = new ModifyGitHubNamePopup(false, GITHUB_NAME_DIALOG_MESSAGE) {
                @Override
                protected void onSaved(KerberosUser modifiedUser) {
                    checkGitHubNameAndCreateContent();
                }
            };
            popup.center();
        } else {
            // get subscription information
            final XsrfTokenServiceAsync xsrf = GWT.create(XsrfTokenService.class);
            ((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "xsrf");
            xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
                @Override
                public void onFailure(Throwable caught) {
                    ExceptionHandler.handle(caught);
                }

                @Override
                public void onSuccess(XsrfToken result) {
                    ((HasRpcToken) gitHubService).setRpcToken(result);
                    gitHubService.getSubscriptions(new AsyncCallback<Set<GithubOrganization>>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            ExceptionHandler.handle("Can't load organizations.", caught);
                        }

                        @Override
                        public void onSuccess(Set<GithubOrganization> result) {
                            loadingPanel.removeFromParent();

                            // add subscription table
                            panel.add(new GitHubNamePanel());
                            panel.add(createSubscriptionTable(result));
                        }
                    });
                }
            });
        }
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
                    if (MembershipStates.NONE.equals(object.getMembershipState())) {
                        return "no";
                    } else if (MembershipStates.ACTIVE.equals(object.getMembershipState())) {
                        return "yes";
                    } else if (MembershipStates.PENDING.equals(object.getMembershipState())) {
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
                    return MembershipStates.NONE.equals(object.getMembershipState())
                            ? "Subscribe" : "Unsubscribe";
                }
            };
            actionColumn.setFieldUpdater(new FieldUpdater<GithubTeam, String>() {
                @Override
                public void update(int index, final GithubTeam object, String value) {
                    if (MembershipStates.NONE.equals(object.getMembershipState())) {
                        gitHubService.subscribe(object.getId(), new AsyncCallback<String>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                ExceptionHandler.handle(caught);
                            }

                            @Override
                            public void onSuccess(String state) {
                                object.setMembershipState(state);
                                cellTable.redraw();
                            }
                        });
                    } else {
                        if (!Window.confirm("Are you sure? You will loose all forked private repositories!")) {
                            return;
                        }
                        gitHubService.unsubscribe(object.getId(), new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                ExceptionHandler.handle(caught);
                            }

                            @Override
                            public void onSuccess(Void result) {
                                object.setMembershipState(MembershipStates.NONE);
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
