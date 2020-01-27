package org.jboss.set.mjolnir.client.application.admin.gitHubMembers;

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
import org.jboss.set.mjolnir.client.NameTokens;
import org.jboss.set.mjolnir.client.application.ApplicationPresenter;
import org.jboss.set.mjolnir.client.application.SplitBundles;
import org.jboss.set.mjolnir.client.application.events.loadingIndicator.LoadingIndicationEvent;
import org.jboss.set.mjolnir.client.application.security.IsAdminGatekeeper;
import org.jboss.set.mjolnir.client.component.ProcessingIndicatorPopup;
import org.jboss.set.mjolnir.client.service.AdministrationService;
import org.jboss.set.mjolnir.client.service.AdministrationServiceAsync;
import org.jboss.set.mjolnir.client.service.DefaultCallback;
import org.jboss.set.mjolnir.shared.domain.GithubOrganization;
import org.jboss.set.mjolnir.shared.domain.GithubTeam;
import org.jboss.set.mjolnir.shared.domain.Subscription;

import java.util.Collection;
import java.util.List;

/**
 * Shows members of configured GitHub organizations.
 *
 * Allows to:
 * * whitelist
 * * unsubscribe from organization
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class GitHubMembersPresenter extends Presenter<GitHubMembersPresenter.MyView, GitHubMembersPresenter.MyProxy>
        implements GitHubMembersHandlers, LoadingIndicationEvent.LoadingIndicatorHandler {

    public interface MyView extends View, HasUiHandlers<GitHubMembersHandlers> {
        void setOrganizations(List<GithubOrganization> items);
        void setSubscriptions(List<Subscription> items);
        List<Subscription> getCurrentSubscriptionList();
        void refresh();
    }

    @ProxyCodeSplitBundle(SplitBundles.ADMIN)
    @NameToken(NameTokens.GITHUB_MEMBERS)
    @UseGatekeeper(IsAdminGatekeeper.class)
    public interface MyProxy extends ProxyPlace<GitHubMembersPresenter> {}

    private AdministrationServiceAsync administrationService = AdministrationService.Util.getInstance();

    @Inject
    public GitHubMembersPresenter(EventBus eventBus, MyView view, MyProxy proxy) {
        super(eventBus, view, proxy, ApplicationPresenter.SLOT_CONTENT);
        getView().setUiHandlers(this);
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        LoadingIndicationEvent.fire(this, true);

        administrationService.getOrganizations(new DefaultCallback<List<GithubOrganization>>() {
            @Override
            public void onSuccess(List<GithubOrganization> result) {
                getView().setOrganizations(result);
                getProxy().manualReveal(GitHubMembersPresenter.this);
            }

            @Override
            public void onFailure(Throwable caught) {
                super.onFailure(caught);
                getProxy().manualRevealFailed();
                LoadingIndicationEvent.fire(GitHubMembersPresenter.this, false);
            }
        });
    }

    @Override
    protected void onBind() {
        super.onBind();

        addRegisteredHandler(LoadingIndicationEvent.TYPE, this);
    }

    @Override
    public void retrieveSubscriptions(GithubOrganization org, GithubTeam team) {
        LoadingIndicationEvent.fire(this, true);

        administrationService.getMembers(org, team, new DefaultCallback<List<Subscription>>() {
            @Override
            public void onSuccess(List<Subscription> subscriptions) {
                getView().setSubscriptions(subscriptions);
                getProxy().manualReveal(GitHubMembersPresenter.this);
                LoadingIndicationEvent.fire(GitHubMembersPresenter.this, false);
            }

            @Override
            public void onFailure(Throwable throwable) {
                super.onFailure(throwable);
                getProxy().manualRevealFailed();
                LoadingIndicationEvent.fire(GitHubMembersPresenter.this, false);
            }
        });
    }

    @Override
    public void unsubscribeUsers(final List<Subscription> selectedItems) {
        administrationService.unsubscribe(selectedItems, new DefaultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // remove selected items from currently displayed list
                getView().getCurrentSubscriptionList().removeAll(selectedItems);
                getView().refresh();
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
                        originalSubscription.setRegisteredUser(subscription.getRegisteredUser());
                    }
                }
                getView().refresh();
            }
        });
    }

    @Override
    public void onLoadingEvent(LoadingIndicationEvent event) {
        if (event.isStart()) {
            ProcessingIndicatorPopup.center();
        } else {
            ProcessingIndicatorPopup.hide();
        }
    }

}
