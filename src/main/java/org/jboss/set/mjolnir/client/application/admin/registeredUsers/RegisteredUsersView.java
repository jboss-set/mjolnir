package org.jboss.set.mjolnir.client.application.admin.registeredUsers;

import java.util.List;

import com.google.inject.Inject;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.jboss.set.mjolnir.client.component.ConfirmationDialog;
import org.jboss.set.mjolnir.client.component.administration.SubscriptionsTable;
import org.jboss.set.mjolnir.client.component.administration.UserDialog;
import org.jboss.set.mjolnir.shared.domain.RegisteredUser;
import org.jboss.set.mjolnir.shared.domain.Subscription;

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

    private SubscriptionsTable subscriptionsTable;

    @Inject
    public RegisteredUsersView() {
        FlowPanel panel = new FlowPanel();
        initWidget(panel);

        panel.add(new HTMLPanel("h2", "Users Registered in Mjolnir"));

        subscriptionsTable = new SubscriptionsTable() {
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
     * Register button delegate.
     */
    private class RegisterHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            //display register dialog
            final UserDialog registerDialog = new UserDialog(null, UserDialog.DialogType.REGISTER) {
                @Override
                protected void onSave(RegisteredUser savedUser, boolean activeAccount) {
                    onRegistered(savedUser, activeAccount);
                }
            };
            registerDialog.center();
        }

        private void onRegistered(RegisteredUser user, boolean isActiveKrb) {
            Subscription subscription = new Subscription();
            subscription.setRegisteredUser(user);
            subscription.setGitHubName(user.getGitHubName());
            subscription.setActiveKerberosAccount(isActiveKrb);
            subscriptionsTable.getItemList().add(subscription);
            subscriptionsTable.refresh();
        }

    }
}
