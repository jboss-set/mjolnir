package org.jboss.set.mjolnir.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.set.mjolnir.shared.domain.EntityUpdateResult;
import org.jboss.set.mjolnir.shared.domain.GithubOrganization;
import org.jboss.set.mjolnir.shared.domain.GithubTeam;
import org.jboss.set.mjolnir.shared.domain.RegisteredUser;
import org.jboss.set.mjolnir.shared.domain.Subscription;
import org.jboss.set.mjolnir.shared.domain.SubscriptionSummary;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public interface AdministrationServiceAsync {

    void getOrganizations(AsyncCallback<List<GithubOrganization>> async);

    void getMembers(GithubOrganization org, GithubTeam team, AsyncCallback<List<Subscription>> async);

    void getOrganizationMembers(AsyncCallback<List<SubscriptionSummary>> async);

    void getRegisteredUsers(AsyncCallback<List<Subscription>> async);

    void checkUserExists(String userName, AsyncCallback<Boolean> async);

    void registerUser(RegisteredUser user, AsyncCallback<EntityUpdateResult<RegisteredUser>> async);

    void deleteUser(RegisteredUser user, AsyncCallback<Void> asyncCallback);

    void deleteUsers(Collection<RegisteredUser> user, AsyncCallback<Void> asyncCallback);

    void editUser(RegisteredUser user, AsyncCallback<EntityUpdateResult<RegisteredUser>> asyncCallback);

    void getSubscriptions(String gitHubName, AsyncCallback<List<GithubOrganization>> asyncCallback);

    void setSubscriptions(String gitHubName, Map<Integer, Boolean> subscriptions, AsyncCallback<Void> asyncCallback);

    void unsubscribe(Collection<Subscription> subscriptions, AsyncCallback<Void> asyncCallback);

    void whitelist(Collection<Subscription> subscriptions, boolean whitelist, AsyncCallback<Collection<Subscription>> asyncCallback);

    void findCurrentGithubUsername(int githubId, AsyncCallback<String> asyncCallback);
}
