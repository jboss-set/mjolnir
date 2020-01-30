package org.jboss.set.mjolnir.client.application.subscriptionSetting;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplitBundle;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.set.mjolnir.client.NameTokens;
import org.jboss.set.mjolnir.client.UIMessages;
import org.jboss.set.mjolnir.client.application.ApplicationPresenter;
import org.jboss.set.mjolnir.client.application.SplitBundles;
import org.jboss.set.mjolnir.client.application.events.loadingIndicator.LoadingIndicationEvent;
import org.jboss.set.mjolnir.client.application.security.CurrentUser;
import org.jboss.set.mjolnir.client.component.NotificationDialog;
import org.jboss.set.mjolnir.client.component.ProcessingIndicatorPopup;
import org.jboss.set.mjolnir.client.service.DefaultCallback;
import org.jboss.set.mjolnir.client.service.GitHubService;
import org.jboss.set.mjolnir.client.service.GitHubServiceAsync;
import org.jboss.set.mjolnir.shared.domain.GithubOrganization;
import org.jboss.set.mjolnir.shared.domain.GithubTeam;
import org.jboss.set.mjolnir.shared.domain.MembershipStates;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows user to subscribe to github teams.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class SubscriptionSettingPresenter
        extends Presenter<SubscriptionSettingPresenter.MyView, SubscriptionSettingPresenter.MyProxy>
        implements SubscribtionHandlers, LoadingIndicationEvent.LoadingIndicatorHandler {

    public interface MyView extends View, HasUiHandlers<SubscribtionHandlers> {
        void setGitHubName(String username);
        void setData(List<GithubOrganization> organizations);
        void refresh();
    }

    @ProxyCodeSplitBundle(SplitBundles.BASE)
    @NameToken(NameTokens.MY_SUBSCRIPTIONS)
    public interface MyProxy extends ProxyPlace<SubscriptionSettingPresenter> {}

    private GitHubServiceAsync gitHubService = GitHubService.Util.getInstance();
    private List<GithubOrganization> organizations;
    private CurrentUser currentUser;
    private PlaceManager placeManager;

    private UIMessages uiMessages = GWT.create(UIMessages.class);

    @Inject
    public SubscriptionSettingPresenter(EventBus eventBus, MyView view, MyProxy proxy, CurrentUser currentUser,
                                        PlaceManager placeManager) {
        super(eventBus, view, proxy, ApplicationPresenter.SLOT_CONTENT);
        this.currentUser = currentUser;
        this.placeManager = placeManager;
        getView().setUiHandlers(this);
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        // show github username form if the username is not set
        if (Strings.isNullOrEmpty(currentUser.getUser().getGitHubName())) {
            getProxy().manualReveal(SubscriptionSettingPresenter.this);
            redirectToGitHubSetting();
        } else {
            // set view data
            loadData();
        }
    }

    @Override
    protected void onBind() {
        super.onBind();

        addRegisteredHandler(LoadingIndicationEvent.TYPE, this);
    }

    private void loadData() {
        getView().setGitHubName(currentUser.getUser().getGitHubName());

        LoadingIndicationEvent.fire(this, true);
        gitHubService.getSubscriptions(new DefaultCallback<List<GithubOrganization>>() {
            @Override
            public void onSuccess(List<GithubOrganization> result) {
                organizations = new ArrayList<>(result);
                getView().setData(organizations);
                LoadingIndicationEvent.fire(SubscriptionSettingPresenter.this, false);
                getProxy().manualReveal(SubscriptionSettingPresenter.this);
            }

            @Override
            public void onFailure(Throwable caught) {
                super.onFailure(caught);

                LoadingIndicationEvent.fire(SubscriptionSettingPresenter.this, false);
                getProxy().manualRevealFailed();
            }
        });
    }

    @Override
    public void subscribe(final GithubTeam team) {
        gitHubService.subscribe(team.getId(), new DefaultCallback<String>() {
            @Override
            public void onSuccess(String result) {
                team.setMembershipState(result);
                getView().refresh();

                String url = uiMessages.organizationUrl(team.getOrganization().getName());
                SafeHtml message = SafeHtmlUtils.fromTrustedString(uiMessages.invitationSentMessage(url));
                new NotificationDialog(uiMessages.invitationSentCaption(), message).center();
            }
        });
    }

    @Override
    public void unsubscribe(final GithubTeam team) {
        gitHubService.unsubscribe(team.getId(), new DefaultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                team.setMembershipState(MembershipStates.NONE);
                getView().refresh();
            }
        });
    }

    @Override
    public void onGitHubNameNotSet() {
        redirectToGitHubSetting();
    }

    private void redirectToGitHubSetting() {
        PlaceRequest placeRequest = new PlaceRequest.Builder()
                .nameToken(NameTokens.GITHUB_SETTING)
                .build();
        placeManager.revealPlace(placeRequest);
    }

    @ProxyEvent
    @Override
    public void onLoadingEvent(LoadingIndicationEvent event) {
        if (event.isStart()) {
            ProcessingIndicatorPopup.center();
        } else {
            ProcessingIndicatorPopup.hide();
        }
    }

}
