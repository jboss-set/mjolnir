package org.jboss.set.mjolnir.client.application.githubSetting;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public interface GitHubSettingHandlers extends UiHandlers {
    void saveSetting(String gitHubName);
}
