package org.jboss.mjolnir.client.application.subscriptionSetting;

import com.gwtplatform.mvp.client.UiHandlers;
import org.jboss.mjolnir.shared.domain.GithubTeam;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public interface SubscribtionHandlers extends UiHandlers {
    void subscribe(GithubTeam team);
    void unsubscribe(GithubTeam team);
//    void modifyGitHubName(String username);
    void onGitHubNameNotSet();
}
