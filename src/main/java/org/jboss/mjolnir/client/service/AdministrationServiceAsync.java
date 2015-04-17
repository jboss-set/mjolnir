package org.jboss.mjolnir.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.mjolnir.authentication.GithubOrganization;
import org.jboss.mjolnir.authentication.KerberosUser;
import org.jboss.mjolnir.client.domain.Subscription;
import org.jboss.mjolnir.client.domain.SubscriptionSummary;
import org.jboss.mjolnir.client.exception.ApplicationException;
import org.jboss.mjolnir.client.exception.GitHubNameAlreadyTakenException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public interface AdministrationServiceAsync {

    void getOrganizationMembers(AsyncCallback<List<SubscriptionSummary>> async) throws ApplicationException;

    void getRegisteredUsers(AsyncCallback<List<Subscription>> async) throws ApplicationException;

    void deleteUser(KerberosUser user, AsyncCallback<Void> asyncCallback) throws ApplicationException;

    void deleteUsers(Collection<KerberosUser> user, AsyncCallback<Void> asyncCallback) throws ApplicationException;

    void editUser(KerberosUser user, AsyncCallback<Void> asyncCallback) throws ApplicationException;

    void getSubscriptions(String gitHubName, AsyncCallback<Set<GithubOrganization>> asyncCallback) throws ApplicationException;

    void setSubscriptions(String gitHubName, Map<Integer, Boolean> subscriptions, AsyncCallback<Void> asyncCallback) throws ApplicationException;

    void unsubscribe(Collection<Subscription> subscriptions, AsyncCallback<Void> asyncCallback) throws ApplicationException;

    void whitelist(Collection<Subscription> subscriptions, boolean whitelist, AsyncCallback<Collection<Subscription>> asyncCallback);
}
