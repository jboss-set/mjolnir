package org.jboss.mjolnir.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.mjolnir.authentication.GithubOrganization;
import org.jboss.mjolnir.authentication.KerberosUser;
import org.jboss.mjolnir.client.domain.Subscription;
import org.jboss.mjolnir.client.domain.SubscriptionSummary;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public interface AdministrationServiceAsync {

    void getOrganizationMembers(AsyncCallback<List<SubscriptionSummary>> async);

    void getRegisteredUsers(AsyncCallback<List<Subscription>> async);

    void deleteUser(KerberosUser user, AsyncCallback<Void> asyncCallback);

    void editUser(KerberosUser user, AsyncCallback<Void> asyncCallback);

    void getSubscriptions(String gitHubName, AsyncCallback<Set<GithubOrganization>> asyncCallback);

    void setSubscriptions(String gitHubName, Map<Integer, Boolean> subscriptions, AsyncCallback<Void> asyncCallback);

}
