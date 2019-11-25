package org.jboss.set.mjolnir.client.component.administration;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import org.jboss.set.mjolnir.client.ExceptionHandler;
import org.jboss.set.mjolnir.client.XsrfUtil;
import org.jboss.set.mjolnir.client.component.LoadingPanel;
import org.jboss.set.mjolnir.client.service.AdministrationService;
import org.jboss.set.mjolnir.client.service.AdministrationServiceAsync;
import org.jboss.set.mjolnir.shared.domain.GithubOrganization;
import org.jboss.set.mjolnir.shared.domain.GithubTeam;
import org.jboss.set.mjolnir.shared.domain.MembershipStates;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class SubscribeUserDialog extends DialogBox {

    private AdministrationServiceAsync administrationService = AdministrationService.Util.getInstance();
    private HTMLPanel checkboxPanel;
    private Map<Integer, CheckBox> checkBoxes = new HashMap<Integer, CheckBox>();
    private String gitHubName;

    public SubscribeUserDialog(final String gitHubName) {
        this.gitHubName = gitHubName;
        setText("GitHub Subscriptions for " + gitHubName);
        setGlassEnabled(true);

        final HTMLPanel panel = new HTMLPanel("");
        panel.setWidth("300px");
        panel.setStyleName("padding");
        setWidget(panel);

//        panel.add(new HTMLPanel("h3", "GitHub Subscriptions for " + gitHubName));

        checkboxPanel = new HTMLPanel("p", "");
        panel.add(checkboxPanel);

        final Anchor selectAllLink = new Anchor("Select All");
        selectAllLink.addClickHandler(new SelectionClickHandler(true));
        panel.add(selectAllLink);
        panel.add(new InlineHTML(" | "));

        final Anchor removeAllLink = new Anchor("Remove All");
        removeAllLink.addClickHandler(new SelectionClickHandler(false));
        panel.add(removeAllLink);

        final HTMLPanel buttonPanel = new HTMLPanel("p", "");
        buttonPanel.setStyleName("textRight");
        panel.add(buttonPanel);

        final Button cancelButton = new Button("Cancel");
        cancelButton.addClickHandler(new CancelClickHandler());
        buttonPanel.add(cancelButton);
        buttonPanel.add(new InlineHTML(" "));

        final Button setButton = new Button("Set Subscriptions");
        setButton.addClickHandler(new SetSubscriptionsClickHandler());
        buttonPanel.add(setButton);
        buttonPanel.add(new InlineHTML(" "));

        // retrieve subscription data and create checkboxes
        createCheckboxes();
    }

    private void createCheckboxes() {
        checkboxPanel.clear();
        checkBoxes.clear();

        checkboxPanel.add(new LoadingPanel());

        XsrfUtil.obtainToken(new XsrfUtil.Callback() {
            @Override
            public void onSuccess(XsrfToken token) {
                ((HasRpcToken) administrationService).setRpcToken(token);
                administrationService.getSubscriptions(gitHubName, new AsyncCallback<List<GithubOrganization>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        ExceptionHandler.handle("Couldn't get user's subscriptions.", caught);
                    }

                    @Override
                    public void onSuccess(List<GithubOrganization> result) {
                        checkboxPanel.clear(); // remove loading indicator

                        for (GithubOrganization organization: result) {
                            checkboxPanel.add(new HTMLPanel("h4", organization.getName()));

                            for (GithubTeam team: organization.getTeams()) {
                                final HTMLPanel item = new HTMLPanel("div", "");
                                final CheckBox checkBox = new CheckBox(team.getName());
                                final boolean value = MembershipStates.ACTIVE.equals(team.getMembershipState())
                                        || MembershipStates.PENDING.equals(team.getMembershipState());
                                checkBox.setValue(value);
                                item.add(checkBox);
                                checkBoxes.put(team.getId(), checkBox);

                                if (MembershipStates.PENDING.equals(team.getMembershipState())) {
                                    final HTMLPanel pendingLabel = new HTMLPanel("span", " *pending*");
                                    pendingLabel.setStyleName("lightText");
                                    item.add(pendingLabel);
                                }

                                checkboxPanel.add(item);
                            }
                        }
                    }
                });
            }
        });
    }

    protected void onSaved() {}

    private Map<Integer, Boolean> getFormData() {
        final Map<Integer, Boolean> data = new HashMap<Integer, Boolean>();
        for (Map.Entry<Integer, CheckBox> entry: checkBoxes.entrySet()) {
            final Integer teamId = entry.getKey();
            final CheckBox checkBox = entry.getValue();
            data.put(teamId, checkBox.getValue());
        }
        return data;
    }

    private class SetSubscriptionsClickHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            XsrfUtil.obtainToken(new XsrfUtil.Callback() {
                @Override
                public void onSuccess(XsrfToken token) {
                    ((HasRpcToken) administrationService).setRpcToken(token);
                    administrationService.setSubscriptions(gitHubName, getFormData(), new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            ExceptionHandler.handle("Couldn't set subscriptions.", caught);
                        }

                        @Override
                        public void onSuccess(Void result) {
                            onSaved();
                            SubscribeUserDialog.this.hide();
                            SubscribeUserDialog.this.removeFromParent();
                        }
                    });
                }
            });
        }
    }

    private class CancelClickHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            SubscribeUserDialog.this.hide();
            SubscribeUserDialog.this.removeFromParent();
        }
    }

    private class SelectionClickHandler implements ClickHandler {

        private boolean select;

        private SelectionClickHandler(boolean select) {
            this.select = select;
        }

        @Override
        public void onClick(ClickEvent event) {
            for (CheckBox checkBox: checkBoxes.values()) {
                checkBox.setValue(select);
            }
        }
    }

}
