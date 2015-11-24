package org.jboss.mjolnir.client.application.admin.registeredUsers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplitBundle;
import com.gwtplatform.mvp.client.annotations.UseGatekeeper;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.mjolnir.client.NameTokens;
import org.jboss.mjolnir.client.application.ApplicationPresenter;
import org.jboss.mjolnir.client.application.SplitBundles;
import org.jboss.mjolnir.client.application.security.IsAdminGatekeeper;
import org.jboss.mjolnir.client.service.AdministrationService;
import org.jboss.mjolnir.client.service.AdministrationServiceAsync;
import org.jboss.mjolnir.client.service.DefaultCallback;
import org.jboss.mjolnir.shared.domain.KerberosUser;
import org.jboss.mjolnir.shared.domain.Subscription;

/**
 * Shows users registered in Mjolnir internal database.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class RegisteredUsersPresenter extends Presenter<RegisteredUsersPresenter.MyView, RegisteredUsersPresenter.MyProxy>
        implements RegisteredUsersHandlers {

    public interface MyView extends View, HasUiHandlers<RegisteredUsersHandlers> {
        void setData(List<Subscription> items);
        List<Subscription> getCurrentSubscriptionList();
        void refresh();
    }

    @ProxyCodeSplitBundle(SplitBundles.ADMIN)
    @NameToken(NameTokens.REGISTERED_USERS)
    @UseGatekeeper(IsAdminGatekeeper.class)
    public interface MyProxy extends ProxyPlace<RegisteredUsersPresenter> {}

    private AdministrationServiceAsync administrationService = AdministrationService.Util.getInstance();

    @Inject
    public RegisteredUsersPresenter(EventBus eventBus, MyView view, MyProxy proxy) {
        super(eventBus, view, proxy, ApplicationPresenter.SLOT_CONTENT);
        getView().setUiHandlers(this);
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        administrationService.getRegisteredUsers(new DefaultCallback<List<Subscription>>() {
            @Override
            public void onSuccess(List<Subscription> result) {
                getView().setData(result);
                getProxy().manualReveal(RegisteredUsersPresenter.this);
            }
        });
    }

    @Override
    public void whitelist(List<Subscription> selectedItems, boolean whitelist) {
        administrationService.whitelist(selectedItems, whitelist, new DefaultCallback<Collection<Subscription>>() {
            @Override
            public void onSuccess(Collection<Subscription> result) {
                // update items in currently displayed list
                List<Subscription> currentSubscriptions = getView().getCurrentSubscriptionList();
                for (Subscription subscription: result) {
                    int idx = currentSubscriptions.indexOf(subscription);
                    if (idx > -1) {
                        Subscription originalSubscription = currentSubscriptions.get(idx);
                        originalSubscription.setKerberosUser(subscription.getKerberosUser());
                    }
                }
                getView().refresh();
            }
        });
    }

    @Override
    public void delete(final List<Subscription> items) {
        // collect list of KerberosUser entities
        final List<KerberosUser> users = new ArrayList<>();
        for (Subscription item : items) {
            KerberosUser user = item.getKerberosUser();
            if (user != null) {
                users.add(user);
            }
        }

        // call delete
        administrationService.deleteUsers(users, new DefaultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                for (Subscription item : items) {
                    // remove object from the list
                    getView().getCurrentSubscriptionList().remove(item);
                }
                getView().refresh();
            }
        });
    }

    @Override
    public void edit(Subscription item) {
        // TODO
    }

    @Override
    public void register(Subscription item) {
        // TODO
    }

}
