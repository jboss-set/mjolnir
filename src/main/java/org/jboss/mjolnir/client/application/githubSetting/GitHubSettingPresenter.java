package org.jboss.mjolnir.client.application.githubSetting;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplitBundle;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.mjolnir.client.NameTokens;
import org.jboss.mjolnir.client.application.ApplicationPresenter;
import org.jboss.mjolnir.client.application.SplitBundles;
import org.jboss.mjolnir.client.application.security.CurrentUser;
import org.jboss.mjolnir.client.service.DefaultCallback;
import org.jboss.mjolnir.client.service.GitHubService;
import org.jboss.mjolnir.client.service.GitHubServiceAsync;
import org.jboss.mjolnir.shared.domain.EntityUpdateResult;
import org.jboss.mjolnir.shared.domain.KerberosUser;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class GitHubSettingPresenter extends Presenter<GitHubSettingPresenter.MyView, GitHubSettingPresenter.MyProxy>
        implements GitHubSettingHandlers {

    public static final String MISSING_NAME_MESSAGE = "We need to know your GitHub username, before you can set modify your subscriptions";

    public interface MyView extends View, HasUiHandlers<GitHubSettingHandlers> {
        void setData(String gitHubName);
        void setFeedbackMessage(List<String> messages);
    }

    @ProxyCodeSplitBundle(SplitBundles.BASE)
    @NameToken(NameTokens.GITHUB_SETTING)
    public interface MyProxy extends ProxyPlace<GitHubSettingPresenter> {
    }

    private GitHubServiceAsync gitHubService = GitHubService.Util.getInstance();
    private CurrentUser currentUser;
    private PlaceManager placeManager;

    @Inject
    public GitHubSettingPresenter(EventBus eventBus, MyView view, MyProxy proxy, PlaceManager placeManager,
                                  CurrentUser currentUser) {
        super(eventBus, view, proxy, ApplicationPresenter.SLOT_CONTENT);
        this.currentUser = currentUser;
        this.placeManager = placeManager;

        getView().setUiHandlers(this);
    }

    @Override
    protected void onReveal() {
        super.onReveal();

        getView().setFeedbackMessage(Collections.<String>emptyList());
        if (currentUser.getUser() != null) {
            String githubName = currentUser.getUser().getGithubName();
            if (Strings.isNullOrEmpty(githubName)) {
                getView().setFeedbackMessage(Collections.singletonList(MISSING_NAME_MESSAGE));
            }
            getView().setData(githubName);
        }
    }

    @Override
    public void saveSetting(String gitHubName) {
        gitHubService.modifyGitHubName(gitHubName, new DefaultCallback<EntityUpdateResult<KerberosUser>>() {
            @Override
            public void onSuccess(EntityUpdateResult<KerberosUser> result) {
                if (result.isOK()) {
                    currentUser.getUser().setGithubName(result.getUpdatedEntity().getGithubName());
                    redirectToSubscriptionScreen();
                } else {
                    getView().setFeedbackMessage(result.getValidationMessages());
                }
            }
        });
    }

    private void redirectToSubscriptionScreen() {
        PlaceRequest placeRequest = new PlaceRequest.Builder()
                .nameToken(NameTokens.MY_SUBSCRIPTIONS)
                .build();
        placeManager.revealPlace(placeRequest);
    }

}
