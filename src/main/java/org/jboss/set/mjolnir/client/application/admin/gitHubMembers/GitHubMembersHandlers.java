package org.jboss.set.mjolnir.client.application.admin.gitHubMembers;

import java.util.List;

import com.gwtplatform.mvp.client.UiHandlers;
import org.jboss.set.mjolnir.shared.domain.Subscription;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public interface GitHubMembersHandlers extends UiHandlers {
    void unsubscribeUsers(List<Subscription> subscriptions);
    void whitelist(List<Subscription> subscriptions, boolean whitelist);
}
