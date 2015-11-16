package org.jboss.mjolnir.client.application.subscriptionSetting;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.mjolnir.shared.domain.GithubOrganization;
import org.jboss.mjolnir.shared.domain.GithubTeam;
import org.jboss.mjolnir.client.NameTokens;
import org.jboss.mjolnir.client.application.ApplicationPresenter;
import org.jboss.mjolnir.client.service.DefaultCallback;
import org.jboss.mjolnir.client.service.GitHubService;
import org.jboss.mjolnir.client.service.GitHubServiceAsync;
import org.jboss.mjolnir.shared.domain.MembershipStates;

/**
 * Allows user to subscribe to github teams.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class SubscriptionSettingPresenter
        extends Presenter<SubscriptionSettingPresenter.MyView, SubscriptionSettingPresenter.MyProxy>
        implements SubscribtionHandlers {

    public interface MyView extends View, HasUiHandlers<SubscribtionHandlers> {
        void setData(List<GithubOrganization> organizations);
        void refresh();
    }

    @ProxyStandard
    @NameToken(NameTokens.MY_SUBSCRIPTIONS)
    public interface MyProxy extends ProxyPlace<SubscriptionSettingPresenter> {}

    private GitHubServiceAsync gitHubService = GitHubService.Util.getInstance();
    private List<GithubOrganization> organizations;

    @Inject
    public SubscriptionSettingPresenter(EventBus eventBus, MyView view, MyProxy proxy) {
        super(eventBus, view, proxy, ApplicationPresenter.SLOT_CONTENT);
        getView().setUiHandlers(this);
    }

    @Override
    protected void onReveal() {
        gitHubService.getSubscriptions(new DefaultCallback<Set<GithubOrganization>>() {
            @Override
            public void onSuccess(Set<GithubOrganization> result) {
                organizations = new ArrayList<>(result);
                getView().setData(organizations);
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

}
