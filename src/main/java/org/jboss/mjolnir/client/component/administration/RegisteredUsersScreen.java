package org.jboss.mjolnir.client.component.administration;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import org.jboss.mjolnir.authentication.KerberosUser;
import org.jboss.mjolnir.client.ExceptionHandler;
import org.jboss.mjolnir.client.XsrfUtil;
import org.jboss.mjolnir.client.component.ConfirmationDialog;
import org.jboss.mjolnir.client.component.LoadingPanel;
import org.jboss.mjolnir.client.domain.Subscription;
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

        Style style = panel.getElement().getStyle();
        style.setHeight(100, Style.Unit.PCT);
        style.setPosition(Style.Position.RELATIVE);

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
        SubscriptionsTable table = new SubscriptionsTable(subscriptions);
        table.addAction("Delete", new DeleteDelegate(table));
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
                    final List<KerberosUser> users = new ArrayList<KerberosUser>();
                    for (Subscription item: selectedItems) {
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
            table.getSelectedItems().clear();
            for (Subscription item: deletedItems) {
                // remove object from the list
                table.getItemList().remove(item);
            }
            table.refresh();
        }
    }
}
