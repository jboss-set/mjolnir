package org.jboss.mjolnir.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.mjolnir.shared.domain.GithubOrganization;
import org.jboss.mjolnir.shared.domain.KerberosUser;
import org.jboss.mjolnir.shared.domain.EntityUpdateResult;
import org.jboss.mjolnir.shared.domain.Subscription;
import org.jboss.mjolnir.shared.domain.SubscriptionSummary;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public interface AdministrationServiceAsync {

    void getOrganizationMembers(AsyncCallback<List<SubscriptionSummary>> async);

    void getRegisteredUsers(AsyncCallback<List<Subscription>> async);

    void checkUserExists(String userName, AsyncCallback<Boolean> async);

    void registerUser(KerberosUser user, AsyncCallback<EntityUpdateResult<KerberosUser>> async);

    void deleteUser(KerberosUser user, AsyncCallback<Void> asyncCallback);

    void deleteUsers(Collection<KerberosUser> user, AsyncCallback<Void> asyncCallback);

    void editUser(KerberosUser user, boolean validateKrbName, boolean validateGHname, AsyncCallback<EntityUpdateResult<KerberosUser>> asyncCallback);

    void getSubscriptions(String gitHubName, AsyncCallback<Set<GithubOrganization>> asyncCallback);

    void setSubscriptions(String gitHubName, Map<Integer, Boolean> subscriptions, AsyncCallback<Void> asyncCallback);

    void unsubscribe(Collection<Subscription> subscriptions, AsyncCallback<Void> asyncCallback);

    void whitelist(Collection<Subscription> subscriptions, boolean whitelist, AsyncCallback<Collection<Subscription>> asyncCallback);
}
