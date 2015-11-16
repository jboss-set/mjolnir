package org.jboss.mjolnir.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.mjolnir.shared.domain.GithubOrganization;
import org.jboss.mjolnir.shared.domain.KerberosUser;
import org.jboss.mjolnir.shared.domain.EntityUpdateResult;

import java.util.Set;

public interface GitHubServiceAsync {

    void getAvailableOrganizations(AsyncCallback<Set<GithubOrganization>> async);

    void getSubscriptions(AsyncCallback<Set<GithubOrganization>> async);

    void unsubscribe(int teamId, AsyncCallback<Void> async);

    void subscribe(int teamId, AsyncCallback<String> async);

    void modifyGitHubName(String newGithubName, AsyncCallback<EntityUpdateResult<KerberosUser>> async);

}
