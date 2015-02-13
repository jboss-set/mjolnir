package org.jboss.mjolnir.client.service;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.server.rpc.XsrfProtect;
import org.jboss.mjolnir.authentication.GithubOrganization;
import org.jboss.mjolnir.authentication.KerberosUser;
import org.jboss.mjolnir.client.domain.Subscription;
import org.jboss.mjolnir.client.domain.SubscriptionSummary;
import org.jboss.mjolnir.client.exception.ApplicationException;
import org.jboss.mjolnir.client.exception.GitHubNameAlreadyTakenException;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service providing administrative tasks.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
@RemoteServiceRelativePath("AdministrationService")
@XsrfProtect
public interface AdministrationService extends RemoteService {

    List<SubscriptionSummary> getOrganizationMembers() throws ApplicationException;

    List<Subscription> getRegisteredUsers() throws ApplicationException;

    void deleteUser(KerberosUser user) throws ApplicationException;

    void editUser(KerberosUser user) throws ApplicationException, GitHubNameAlreadyTakenException;

    Set<GithubOrganization> getSubscriptions(String gitHubName) throws ApplicationException;

    void setSubscriptions(String gitHubName, Map<Integer, Boolean> subscriptions) throws ApplicationException;

    public static class Util {
        private static AdministrationServiceAsync instance;

        public static AdministrationServiceAsync getInstance() {
            if (instance == null) {
                instance = GWT.create(AdministrationService.class);
            }
            return instance;
        }
    }
}
