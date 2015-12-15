package org.jboss.mjolnir.client.application;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import org.jboss.mjolnir.client.application.admin.gitHubMembers.GitHubMembersPresenter;
import org.jboss.mjolnir.client.application.admin.gitHubMembers.GitHubMembersView;
import org.jboss.mjolnir.client.application.admin.registeredUsers.RegisteredUsersPresenter;
import org.jboss.mjolnir.client.application.admin.registeredUsers.RegisteredUsersView;
import org.jboss.mjolnir.client.application.error.ErrorPresenter;
import org.jboss.mjolnir.client.application.error.ErrorView;
import org.jboss.mjolnir.client.application.githubSetting.GitHubSettingPresenter;
import org.jboss.mjolnir.client.application.githubSetting.GitHubSettingView;
import org.jboss.mjolnir.client.application.subscriptionSetting.SubscriptionSettingPresenter;
import org.jboss.mjolnir.client.application.subscriptionSetting.SubscriptionSettingView;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class ApplicationModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(ApplicationPresenter.class, ApplicationPresenter.MyView.class, ApplicationView.class,
                ApplicationPresenter.MyProxy.class);

        bindPresenter(ErrorPresenter.class, ErrorPresenter.MyView.class, ErrorView.class,
                ErrorPresenter.MyProxy.class);

        bindPresenter(SubscriptionSettingPresenter.class, SubscriptionSettingPresenter.MyView.class,
                SubscriptionSettingView.class,
                SubscriptionSettingPresenter.MyProxy.class);

        bindPresenter(GitHubSettingPresenter.class, GitHubSettingPresenter.MyView.class,
                GitHubSettingView.class,
                GitHubSettingPresenter.MyProxy.class);

        bindPresenter(GitHubMembersPresenter.class, GitHubMembersPresenter.MyView.class,
                GitHubMembersView.class,
                GitHubMembersPresenter.MyProxy.class);

        bindPresenter(RegisteredUsersPresenter.class, RegisteredUsersPresenter.MyView.class,
                RegisteredUsersView.class,
                RegisteredUsersPresenter.MyProxy.class);
    }
}
