package org.jboss.mjolnir.client.component.administration;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import org.jboss.mjolnir.shared.domain.KerberosUser;
import org.jboss.mjolnir.client.ExceptionHandler;
import org.jboss.mjolnir.client.XsrfUtil;
import org.jboss.mjolnir.client.component.ConfirmationDialog;
import org.jboss.mjolnir.client.component.LoadingPanel;
import org.jboss.mjolnir.client.component.table.ConditionalActionCell;
import org.jboss.mjolnir.shared.domain.Subscription;
import org.jboss.mjolnir.client.service.AdministrationService;
import org.jboss.mjolnir.client.service.AdministrationServiceAsync;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Screen showing list of users registered in Mjolnir.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class RegisteredUsersScreen extends Composite {

    private static final String DELETE_USER_SUBTEXT = "Note: this will not remove users' subscriptions on GitHub.";

    private static final Logger logger = Logger.getLogger(SubscriptionsTable.class.getName());

    private AdministrationServiceAsync administrationService = AdministrationService.Util.getInstance();
    private HTMLPanel panel = new HTMLPanel("");
    private LoadingPanel loadingPanel = new LoadingPanel();


    public RegisteredUsersScreen() {
        initWidget(panel);

        panel.add(new HTMLPanel("h2", "Users Registered in Mjolnir"));
        panel.add(loadingPanel);

        XsrfUtil.obtainToken(new XsrfUtil.Callback() {
            @Override
            public void onSuccess(XsrfToken token) {
                ((HasRpcToken) administrationService).setRpcToken(token);
                administrationService.getRegisteredUsers(new AsyncCallback<List<Subscription>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        ExceptionHandler.handle("Couldn't get registered users.", caught);
                    }

                    @Override
                    public void onSuccess(List<Subscription> result) {
                        loadingPanel.removeFromParent();
                        panel.add(createTable(result));
                    }
                });
            }
        });
    }

    private SubscriptionsTable createTable(List<Subscription> subscriptions) {
        SubscriptionsTable table = new SubscriptionsTable(subscriptions) {
            protected void addDefaultActionCells() {
                // edit button
                addActionCell(new ConditionalActionCell<>(SafeHtmlUtils.fromString("Edit"), new EditDelegate(this)));
                super.addDefaultActionCells();
            }
        };
        table.addAction("Delete", new DeleteDelegate(table), true, false);
        table.addAction("Register", new RegisterDelegate(table), true, true);
        return table;
    }

    private class DeleteDelegate implements SubscriptionsTable.ActionDelegate {

        private SubscriptionsTable table;

        public DeleteDelegate(SubscriptionsTable table) {
            this.table = table;
        }

        @Override
        public void execute(final List<Subscription> selectedItems) {
            ConfirmationDialog dialog =
                    new ConfirmationDialog("Delete " + selectedItems.size() + " users from Mjolnir database?", DELETE_USER_SUBTEXT) {
                        @Override
                        public void onConfirm() {
                            final List<KerberosUser> users = new ArrayList<>();
                            for (Subscription item : selectedItems) {
                                KerberosUser user = item.getKerberosUser();
                                if (user != null) {
                                    users.add(user);
                                }
                            }

                            logger.info("Deleting items " + Arrays.toString(users.toArray()));

                            administrationService.deleteUsers(users, new AsyncCallback<Void>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    ExceptionHandler.handle(caught);
                                }

                                @Override
                                public void onSuccess(Void result) {
                                    onDeleted(selectedItems);
                                }
                            });
                        }
                    };
            dialog.center();
        }

        private void onDeleted(Collection<Subscription> deletedItems) {
            for (Subscription item : deletedItems) {
                // remove object from the list
                table.getItemList().remove(item);
            }
            table.refresh();
        }
    }

    /**
     * Edit button delegate.
     */
    private class EditDelegate implements ActionCell.Delegate<Subscription> {

        private SubscriptionsTable table;

        public EditDelegate(SubscriptionsTable table) {
            this.table = table;
        }

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
                protected void onSave(KerberosUser savedUser) {
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
            table.getDataProvider().refresh();
        }

    }

    /**
     * Register button delegate.
     */
    private class RegisterDelegate implements SubscriptionsTable.ActionDelegate {

        private SubscriptionsTable table;

        public RegisterDelegate(SubscriptionsTable table) {
            this.table = table;
        }

        @Override
        public void execute(final List<Subscription> selectedItems) {
            //display register dialog
            final UserDialog registerDialog = new UserDialog(null, UserDialog.DialogType.REGISTER) {
                @Override
                protected void onSave(KerberosUser savedUser) {
                    onRegistered(savedUser, activeAccountCheckBox.getValue());
                }
            };
            registerDialog.center();
        }

        private void onRegistered(KerberosUser user, boolean isActiveKrb) {
            Subscription subscription = new Subscription();
            subscription.setKerberosUser(user);
            subscription.setGitHubName(user.getGithubName());
            subscription.setActiveKerberosAccount(isActiveKrb);
            table.getItemList().add(subscription);
            table.refresh();
        }
    }

}
