package org.jboss.mjolnir.client.application.admin.registeredUsers;

import java.util.List;

import javax.inject.Inject;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.jboss.mjolnir.client.component.ConfirmationDialog;
import org.jboss.mjolnir.client.component.administration.SubscriptionsTable2;
import org.jboss.mjolnir.client.component.administration.UserDialog;
import org.jboss.mjolnir.client.component.table.ConditionalActionCell;
import org.jboss.mjolnir.shared.domain.KerberosUser;
import org.jboss.mjolnir.shared.domain.Subscription;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class RegisteredUsersView extends ViewWithUiHandlers<RegisteredUsersHandlers> implements RegisteredUsersPresenter.MyView {

    interface Templates extends SafeHtmlTemplates {
        @Template("Delete {0} registered users from Mjolnir?")
        SafeHtml deleteUsers(int number);

        @Template("Note: this will not remove users' subscriptions on GitHub.")
        SafeHtml subscriptionsWillNotBeRemoved();
    }
    private static final Templates TEMPLATES = GWT.create(Templates.class);

    private SubscriptionsTable2 subscriptionsTable;

    @Inject
    public RegisteredUsersView() {
        FlowPanel panel = new FlowPanel();
        initWidget(panel);

        panel.add(new HTMLPanel("h2", "Users Registered in Mjolnir"));

        subscriptionsTable = new SubscriptionsTable2() {
            @Override
            protected void addDefaultActionCells() {
                // edit button
                addActionCell(new ConditionalActionCell<>(SafeHtmlUtils.fromString("Edit"), new EditDelegate()));
                super.addDefaultActionCells();
            }

            @Override
            protected void dispatchWhitelist(List<Subscription> selectedItems, boolean whitelist) {

            }
        };
        subscriptionsTable.addAction("Delete", new DeleteClickHandler(), true, false);
        subscriptionsTable.addAction("Register", new RegisterHandler(), true, true);
        panel.add(subscriptionsTable);
    }

    @Override
    public void setData(List<Subscription> items) {
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


    private class DeleteClickHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            final List<Subscription> selectedItems = subscriptionsTable.getSelectedItems();

            ConfirmationDialog dialog =
                    new ConfirmationDialog(TEMPLATES.deleteUsers(selectedItems.size()).asString(),
                            TEMPLATES.subscriptionsWillNotBeRemoved().asString()) {
                        @Override
                        public void onConfirm() {
                            getUiHandlers().delete(selectedItems);
                        }
                    };
            dialog.center();
        }

    }

    /**
     * Edit button delegate.
     */
    private class EditDelegate implements ActionCell.Delegate<Subscription> {

        @Override
        public void execute(final Subscription object) {
            // displays edit dialog
            KerberosUser userToEdit = object.getKerberosUser();
            if (userToEdit == null) { // if user is not yet in our database, create new object
                userToEdit = new KerberosUser();
                userToEdit.setGithubName(object.getGitHubName());
            }
            final UserDialog editDialog = new UserDialog(userToEdit, UserDialog.DialogType.EDIT) {
                @Override
                protected void onSave(KerberosUser savedUser, boolean activeAccount) {
                    onEdited(object, savedUser);
                }
            };
            editDialog.center();
        }


        /**
         * Called after item was modified.
         *
         * @param object    modified item
         * @param savedUser user instance that was actually saved on server
         */
        protected void onEdited(Subscription object, KerberosUser savedUser) {
            // updates subscription item in the list with current user object
            object.setKerberosUser(savedUser);
            object.setGitHubName(savedUser.getGithubName());
            subscriptionsTable.refresh();
        }

    }

    /**
     * Register button delegate.
     */
    private class RegisterHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            //display register dialog
            final UserDialog registerDialog = new UserDialog(null, UserDialog.DialogType.REGISTER) {
                @Override
                protected void onSave(KerberosUser savedUser, boolean activeAccount) {
                    onRegistered(savedUser, activeAccount);
                }
            };
            registerDialog.center();
        }

        private void onRegistered(KerberosUser user, boolean isActiveKrb) {
            Subscription subscription = new Subscription();
            subscription.setKerberosUser(user);
            subscription.setGitHubName(user.getGithubName());
            subscription.setActiveKerberosAccount(isActiveKrb);
            subscriptionsTable.getItemList().add(subscription);
            subscriptionsTable.refresh();
        }

    }
}
