package org.jboss.set.mjolnir.client.service;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.server.rpc.XsrfProtect;
import org.jboss.set.mjolnir.client.XsrfUtil;
import org.jboss.set.mjolnir.client.exception.ApplicationException;
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
 * Service providing administrative tasks.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
@RemoteServiceRelativePath("auth/AdministrationService")
@XsrfProtect
public interface AdministrationService extends RemoteService {

    List<GithubOrganization> getOrganizations() throws ApplicationException;

    List<Subscription> getMembers(GithubOrganization org, GithubTeam team);

    List<SubscriptionSummary> getOrganizationMembers() throws ApplicationException;

    List<Subscription> getRegisteredUsers() throws ApplicationException;

    Boolean checkUserExists(String userName);

    EntityUpdateResult<RegisteredUser> registerUser(RegisteredUser user) throws ApplicationException;

    void deleteUser(RegisteredUser user) throws ApplicationException;

    void deleteUsers(Collection<RegisteredUser> user) throws ApplicationException;

    EntityUpdateResult<RegisteredUser> editUser(RegisteredUser user) throws ApplicationException;

    List<GithubOrganization> getSubscriptions(String gitHubName) throws ApplicationException;

    void unsubscribe(Collection<Subscription> subscriptions) throws ApplicationException;

    void setSubscriptions(String gitHubName, Map<Integer, Boolean> subscriptions) throws ApplicationException;

    Collection<Subscription> whitelist(Collection<Subscription> subscriptions, boolean whitelist);

    String findCurrentGithubUsername(int githubId);

    class Util {
        private static AdministrationServiceAsync instance;

        public static AdministrationServiceAsync getInstance() {
            if (instance == null) {
                instance = GWT.create(AdministrationService.class);
            }
            XsrfUtil.putToken((HasRpcToken) instance);
            return instance;
        }
    }
}
