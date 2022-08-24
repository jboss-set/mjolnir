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
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Combined dialog for editing RegisteredUser and his subscriptions.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public abstract class UserAndSubscriptionsUserDialog extends DialogBox {

    private static final Logger log = Logger.getLogger("");

    private static final String RESPONSIBLE_PERSON_ROW_ID = "Responsibleperson";

    private final AdministrationServiceAsync administrationService = AdministrationService.Util.getInstance();
    private final HTMLPanel checkboxPanel;
    private final Map<Integer, CheckBox> checkBoxes = new HashMap<>();
    private final Subscription subscription;

    private final TextBox krbNameBox = new TextBox();
    // GH username provided by user or admin
    private final TextBox githubNameBox = new TextBox();
    // current GH username based on GH ID, which can be different from above if the user renamed his acount
    private final TextBox currentGithubNameBox = new TextBox();
    private final TextBox githubIdBox = new TextBox();
    private final TextBox noteBox = new TextBox();
    private final TextBox responsiblePersonBox = new TextBox();
    private final CheckBox adminCheckBox = new CheckBox();
    private final CheckBox whitelistedCheckBox = new CheckBox();
    private final HTML feedback = new HTML();

    private String discoveredGithubName;

    public UserAndSubscriptionsUserDialog(final Subscription subscription) {
        this.subscription = subscription;

        githubIdBox.setReadOnly(true);
        githubIdBox.setEnabled(false);
        currentGithubNameBox.setReadOnly(true);
        currentGithubNameBox.setEnabled(false);

        feedback.getElement().addClassName("error");

        RegisteredUser registeredUser = subscription.getRegisteredUser();
        githubNameBox.setText(subscription.getGitHubName());
        if (registeredUser != null) {
            krbNameBox.setValue(registeredUser.getKrbName());
            noteBox.setValue(registeredUser.getNote());
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

        setText("User Details for " + subscription.getKrbName());
        setGlassEnabled(true);

        final HTMLPanel panel = new HTMLPanel("");
        panel.setWidth("500px");
        panel.getElement().addClassName("padding");
        panel.getElement().addClassName("form");
        setWidget(panel);

        panel.add(new HTMLPanel("h3", "User details"));

        HTMLPanel userForm = new HTMLPanel("p", "");
        panel.add(userForm);
        userForm.add(createRow("Kerberos Name", "User name in company Kerberos database", krbNameBox));
        userForm.add(createRow("Registered GitHub Name", "User's GitHub account name provided during registration", githubNameBox));
        userForm.add(createRow("Current GitHub Name", "User's GitHub account name based on his GitHub ID", currentGithubNameBox));
        userForm.add(createRow("GitHub ID", "User's GitHub account ID, automatically retrieved during registration", githubIdBox));
        userForm.add(createRow("Note", "Additional notes about the user", noteBox));
        userForm.add(createRow("Admin", "Does user have admin privileges?", adminCheckBox));
        userForm.add(createRow("Whitelisted", "If true, this user will not appear in the email report of users without an active kerberos account.", whitelistedCheckBox));
        userForm.add(createRow("Responsible person", "Responsible person", responsiblePersonBox,
                registeredUser != null && registeredUser.isWhitelisted()));
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

        if (subscription.getGitHubId() != null) {
            githubIdBox.setValue(subscription.getGitHubId().toString());
            retrieveCurrentGithubUsername(subscription.getGitHubId(), (githubUsername) -> {
                discoveredGithubName = githubUsername;
                currentGithubNameBox.setValue(githubUsername);
                createSubscriptionFormPart(githubUsername);
            });
        }
    }

    /**
     * Creates part of the form with checkboxes that display which teams user is subscribed to,
     * and allow to change the subscriptions.
     *
     * @param currentGithubName GH username (an up-to-date username should be given, not the possibly deprecated
     *                          value which user provided during registration)
     */
    private void createSubscriptionFormPart(String currentGithubName) {
        checkboxPanel.clear();
        checkBoxes.clear();

        if (currentGithubName == null) {
            return;
        }

        checkboxPanel.add(new LoadingPanel());

        XsrfUtil.obtainToken(new XsrfUtil.Callback() {
            @Override
            public void onSuccess(XsrfToken token) {
                ((HasRpcToken) administrationService).setRpcToken(token);
                administrationService.getSubscriptions(currentGithubName, new AsyncCallback<List<GithubOrganization>>() {
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
        saveUserDetails(this::saveSubscriptions);
    }

    private void saveUserDetails(final Runnable nextStep) {
        RegisteredUser userToSave = subscription.getRegisteredUser() == null
                ? new RegisteredUser() : subscription.getRegisteredUser().copy();
        userToSave.setGitHubName(githubNameBox.getValue());
        userToSave.setKrbName(krbNameBox.getValue());
        userToSave.setNote(noteBox.getValue());
        userToSave.setAdmin(adminCheckBox.getValue());
        userToSave.setWhitelisted(whitelistedCheckBox.getValue());
        userToSave.setResponsiblePerson(responsiblePersonBox.getValue());

        if (subscription.getRegisteredUser() != null) {
            // modify existing user
            administrationService.editUser(userToSave, new AsyncCallback<EntityUpdateResult<RegisteredUser>>() {
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
            RegisteredUser updatedUser = result.getUpdatedEntity();
            RegisteredUser displayedUser = subscription.getRegisteredUser();
            updatedUser.copyTo(displayedUser);
            subscription.setGitHubId(updatedUser.getGitHubId());
            subscription.setGitHubName(updatedUser.getGitHubName());
            onUserSavedCallback(updatedUser);

            // update subscriptins
            nextStep.run();
        } else {
            feedback.setHTML(HTMLUtil.toUl(result.getValidationMessages()));
        }
    }

    private void saveSubscriptions() {
        if (isNotBlank(discoveredGithubName)) {
            administrationService.setSubscriptions(discoveredGithubName, getSubscriptionData(),
                    new AsyncCallback<Void>() {
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

    /**
     * Retrieves current GH username based on GH ID, and provides an action.
     *
     * @param githubId GH ID of a user
     * @param onSuccess a consumer that accepts the discovered GH username
     */
    private void retrieveCurrentGithubUsername(int githubId, Consumer<String> onSuccess) {
        log.info("Retrieving current GH username for ID " + githubId);
        administrationService.findCurrentGithubUsername(githubId, new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                log.log(Level.SEVERE, "Can't retrieve GH username for ID " + githubId,  caught);
            }

            @Override
            public void onSuccess(String githubUsername) {
                log.info("Current GH username for ID " + githubId + " is " + githubUsername);
                onSuccess.accept(githubUsername);
            }
        });
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

    private static boolean isNotBlank(String string) {
        return string != null
                && !string.isEmpty();
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

        private final boolean select;

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
