package org.jboss.mjolnir.client.application.subscriptionSetting;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplitBundle;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.mjolnir.client.NameTokens;
import org.jboss.mjolnir.client.application.ApplicationPresenter;
import org.jboss.mjolnir.client.application.SplitBundles;
import org.jboss.mjolnir.client.application.security.CurrentUser;
import org.jboss.mjolnir.client.component.ModifyGitHubNamePopup;
import org.jboss.mjolnir.client.service.DefaultCallback;
import org.jboss.mjolnir.client.service.GitHubService;
import org.jboss.mjolnir.client.service.GitHubServiceAsync;
import org.jboss.mjolnir.shared.domain.EntityUpdateResult;
import org.jboss.mjolnir.shared.domain.GithubOrganization;
import org.jboss.mjolnir.shared.domain.GithubTeam;
import org.jboss.mjolnir.shared.domain.KerberosUser;
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
        void setGitHubName(String username);
        void setData(List<GithubOrganization> organizations);
        void refresh();
        ModifyGitHubNamePopup getGitHubNamePopup();
    }

    @ProxyCodeSplitBundle(SplitBundles.BASE)
    @NameToken(NameTokens.MY_SUBSCRIPTIONS)
    public interface MyProxy extends ProxyPlace<SubscriptionSettingPresenter> {}

    private GitHubServiceAsync gitHubService = GitHubService.Util.getInstance();
    private List<GithubOrganization> organizations;
    private CurrentUser currentUser;

    @Inject
    public SubscriptionSettingPresenter(EventBus eventBus, MyView view, MyProxy proxy, CurrentUser currentUser) {
        super(eventBus, view, proxy, ApplicationPresenter.SLOT_CONTENT);
        this.currentUser = currentUser;
        getView().setUiHandlers(this);
    }

    @Override
    protected void onReveal() {
        // show github username form if the username is not set
        if (Strings.isNullOrEmpty(currentUser.getUser().getGithubName())) {
            getView().getGitHubNamePopup().enableCancelButton(false);
            getView().getGitHubNamePopup().center();
        } else {
            // set view data
            loadData();
        }

    }

    private void loadData() {
        getView().setGitHubName(currentUser.getUser().getGithubName());

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

    @Override
    public void modifyGitHubName(final String username) {
        gitHubService.modifyGitHubName(username, new DefaultCallback<EntityUpdateResult<KerberosUser>>() {
            @Override
            public void onSuccess(EntityUpdateResult<KerberosUser> result) {
                if (result.isOK()) {
                    getView().setGitHubName(result.getUpdatedEntity().getGithubName());
                    getView().getGitHubNamePopup().success();
                    currentUser.getUser().setGithubName(result.getUpdatedEntity().getGithubName());
                    loadData();
                } else {
                    getView().getGitHubNamePopup().validationError(result.getValidationMessages());
                }
            }
        });
    }

}
