package org.jboss.set.mjolnir.client.component.administration;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.set.mjolnir.client.ExceptionHandler;
import org.jboss.set.mjolnir.client.XsrfUtil;
import org.jboss.set.mjolnir.client.component.LoadingPanel;
import org.jboss.set.mjolnir.client.component.util.HTMLUtil;
import org.jboss.set.mjolnir.client.service.AdministrationService;
import org.jboss.set.mjolnir.client.service.AdministrationServiceAsync;
import org.jboss.set.mjolnir.shared.domain.EntityUpdateResult;
import org.jboss.set.mjolnir.shared.domain.GithubOrganization;
import org.jboss.set.mjolnir.shared.domain.GithubTeam;
import org.jboss.set.mjolnir.shared.domain.MembershipStates;
import org.jboss.set.mjolnir.shared.domain.RegisteredUser;
import org.jboss.set.mjolnir.shared.domain.Subscription;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Combined dialog for editing RegisteredUser and his subscriptions.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public abstract class UserAndSubscriptionsUserDialog extends DialogBox {

    private static final String RESPONSIBLE_PERSON_ROW_ID = "Responsibleperson";

    private AdministrationServiceAsync administrationService = AdministrationService.Util.getInstance();
    private HTMLPanel checkboxPanel;
    private Map<Integer, CheckBox> checkBoxes = new HashMap<>();
    private Subscription subscription;

    private TextBox krbNameBox = new TextBox();
    private TextBox githubNameBox = new TextBox();
    private TextBox noteBox = new TextBox();
    private TextBox responsiblePersonBox = new TextBox();
    private CheckBox adminCheckBox = new CheckBox();
    private CheckBox whitelistedCheckBox = new CheckBox();
    private HTML feedback = new HTML();

    public UserAndSubscriptionsUserDialog(final Subscription subscription) {
        this.subscription = subscription;

        githubNameBox.setEnabled(false);
        feedback.getElement().addClassName("error");

        RegisteredUser registeredUser = subscription.getRegisteredUser();
        githubNameBox.setText(subscription.getGitHubName());
        if (registeredUser != null) {
            krbNameBox.setText(registeredUser.getKrbName());
            noteBox.setText(registeredUser.getNote());
            adminCheckBox.setValue(registeredUser.isAdmin());
            whitelistedCheckBox.setValue(registeredUser.isWhitelisted());
            responsiblePersonBox.setValue(registeredUser.getResponsiblePerson());
        }

        whitelistedCheckBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                CheckBox checkBox = (CheckBox)event.getSource();
                if (checkBox.getValue())
                    DOM.getElementById(RESPONSIBLE_PERSON_ROW_ID).getStyle().setDisplay(Style.Display.BLOCK);
                else
                    DOM.getElementById(RESPONSIBLE_PERSON_ROW_ID).getStyle().setDisplay(Style.Display.NONE);
            }
        });

        setText("User Details for " + subscription.getGitHubName());
        setGlassEnabled(true);

        final HTMLPanel panel = new HTMLPanel("");
        panel.setWidth("500px");
        panel.getElement().addClassName("padding");
        panel.getElement().addClassName("form");
        setWidget(panel);

        panel.add(new HTMLPanel("h3", "User details"));

        HTMLPanel userForm = new HTMLPanel("p", "");
        panel.add(userForm);
        userForm.add(createRow("GitHub Name", "User's GitHub account name", githubNameBox));
        userForm.add(createRow("Kerberos Name", "User name in company Kerberos database", krbNameBox));
        userForm.add(createRow("Note", "Additional notes about the user", noteBox));
        userForm.add(createRow("Admin", "Does user have admin privileges?", adminCheckBox));
        userForm.add(createRow("Whitelisted", "If true, this user will not appear in the email report of users without an active kerberos account.", whitelistedCheckBox));
        userForm.add(createRow("Responsible person", "Responsible person", responsiblePersonBox, registeredUser.isWhitelisted()));
        userForm.add(feedback);

        panel.add(new HTMLPanel("h3", "GitHub Subscriptions"));

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

        final Button saveButton = new Button("Save changes");
        saveButton.addClickHandler(new SaveClickHandler());
        buttonPanel.add(saveButton);
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
                administrationService.getSubscriptions(subscription.getGitHubName(), new AsyncCallback<List<GithubOrganization>>() {
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

    private void saveData() {
        saveUserDetails(new Runnable() {
            @Override
            public void run() {
                saveSubscriptions();
            }
        });
    }

    private void saveUserDetails(final Runnable nextStep) {
        RegisteredUser userToSave = subscription.getRegisteredUser() == null
                ? new RegisteredUser() : subscription.getRegisteredUser().copy();
        userToSave.setGitHubName(subscription.getGitHubName());
        userToSave.setKrbName(krbNameBox.getText());
        userToSave.setNote(noteBox.getText());
        userToSave.setAdmin(adminCheckBox.getValue());
        userToSave.setWhitelisted(whitelistedCheckBox.getValue());
        userToSave.setResponsiblePerson(responsiblePersonBox.getValue());

        if (subscription.getRegisteredUser() != null) {
            // modify existing user
            administrationService.editUser(userToSave, true, true, new AsyncCallback<EntityUpdateResult<RegisteredUser>>() {
                @Override
                public void onFailure(Throwable caught) {
                    ExceptionHandler.handle("Couldn't edit user.", caught);
                }

                @Override
                public void onSuccess(EntityUpdateResult<RegisteredUser> result) {
                    userSaveSuccess(result, nextStep);
                }
            });
        } else {
            // insert new user
            administrationService.registerUser(userToSave, new AsyncCallback<EntityUpdateResult<RegisteredUser>>() {
                @Override
                public void onFailure(Throwable caught) {
                    ExceptionHandler.handle("Couldn't create user.", caught);
                }

                @Override
                public void onSuccess(EntityUpdateResult<RegisteredUser> result) {
                    userSaveSuccess(result, nextStep);
                }
            });
        }
    }

    private void userSaveSuccess(EntityUpdateResult<RegisteredUser> result, Runnable nextStep) {
        if (result.isOK()) {
            // update original user instance
            if (subscription.getRegisteredUser() == null) {
                subscription.setRegisteredUser(new RegisteredUser());
            }
            result.getUpdatedEntity().copyTo(subscription.getRegisteredUser());
            onUserSavedCallback(result.getUpdatedEntity());

            // update subscriptins
            nextStep.run();
        } else {
            feedback.setHTML(HTMLUtil.toUl(result.getValidationMessages()));
        }
    }

    private void saveSubscriptions() {
        administrationService.setSubscriptions(this.subscription.getGitHubName(), getSubscriptionData(), new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                ExceptionHandler.handle("Couldn't set subscriptions.", caught);
            }

            @Override
            public void onSuccess(Void result) {
                UserAndSubscriptionsUserDialog.this.hide();
                UserAndSubscriptionsUserDialog.this.removeFromParent();
            }
        });
    }

    private Map<Integer, Boolean> getSubscriptionData() {
        final Map<Integer, Boolean> data = new HashMap<>();
        for (Map.Entry<Integer, CheckBox> entry: checkBoxes.entrySet()) {
            final Integer teamId = entry.getKey();
            final CheckBox checkBox = entry.getValue();
            data.put(teamId, checkBox.getValue());
        }
        return data;
    }

    private static Panel createRow(String label, String helpText, Widget formItem) {
        return createRow(label, helpText, formItem, true);
    }

    private static Panel createRow(String label, String helpText, Widget formItem, boolean visible) {
        HTMLPanel row = new HTMLPanel("div", "");
        row.getElement().setId(label.replaceAll("\\s+",""));
        row.getElement().addClassName("row");
        Anchor helpLink;
        final Panel helpTextPanel;
        row.add(createLabel(label));
        row.add(formItem);
        row.add(helpLink = createHelpLink());
        row.add(helpTextPanel = createHelpText(helpText));

        if (!visible)
            row.getElement().getStyle().setDisplay(Style.Display.NONE);

        helpLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String currentValue = helpTextPanel.getElement().getStyle().getDisplay();
                helpTextPanel.getElement().getStyle().setDisplay(Style.Display.NONE.getCssName().equals(currentValue)
                        ? Style.Display.BLOCK : Style.Display.NONE);
            }
        });

        return row;
    }

    private static Anchor createHelpLink() {
        Anchor anchor = new Anchor("?");
        anchor.getElement().addClassName("help-link");
        return anchor;
    }

    private static Panel createLabel(String label) {
        HTMLPanel span = new HTMLPanel("span", label);
        span.getElement().addClassName("label");
        return span;
    }

    private static Panel createHelpText(String helpText) {
        HTMLPanel div = new HTMLPanel("div", helpText);
        div.getElement().addClassName("help-text");
        div.getElement().getStyle().setDisplay(Style.Display.NONE);
        return div;
    }

    private class SaveClickHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            feedback.setHTML("");
            XsrfUtil.obtainToken(new XsrfUtil.Callback() {
                @Override
                public void onSuccess(XsrfToken token) {
                    ((HasRpcToken) administrationService).setRpcToken(token);
                    saveData();
                }
            });
        }
    }

    protected abstract void onUserSavedCallback(RegisteredUser registeredUser);

    private class CancelClickHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            UserAndSubscriptionsUserDialog.this.hide();
            UserAndSubscriptionsUserDialog.this.removeFromParent();
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
