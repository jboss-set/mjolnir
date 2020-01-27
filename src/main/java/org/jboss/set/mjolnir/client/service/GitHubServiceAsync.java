package org.jboss.set.mjolnir.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.set.mjolnir.shared.domain.EntityUpdateResult;
import org.jboss.set.mjolnir.shared.domain.GithubOrganization;
import org.jboss.set.mjolnir.shared.domain.RegisteredUser;

import java.util.List;

public interface GitHubServiceAsync {

    void getAvailableOrganizations(AsyncCallback<List<GithubOrganization>> async);

    void getSubscriptions(AsyncCallback<List<GithubOrganization>> async);

    void unsubscribe(int teamId, AsyncCallback<Void> async);

    void subscribe(int teamId, AsyncCallback<String> async);

    void modifyGitHubName(String newGithubName, AsyncCallback<EntityUpdateResult<RegisteredUser>> async);

}
