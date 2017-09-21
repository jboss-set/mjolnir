package org.jboss.set.mjolnir.server.service.statuscheck;

import java.io.IOException;

import javax.ejb.Singleton;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.IGitHubConstants;

/**
 * Checks that GitHub is reachable.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
@Singleton
public class GitHubStatusCheck extends AbstractStatusCheck {

    private static final String TITLE = "GitHub";
    private String hostName = IGitHubConstants.HOST_API;

    public GitHubStatusCheck() {
        super(TITLE);
    }

    @Override
    protected StatusCheckResult doCheckStatus() throws Exception {
        StatusCheckResult result = new StatusCheckResult();

        GitHubClient client = new GitHubClient(hostName);
        GitHubRequest request = new GitHubRequest();
        request.setUri("/");

        try {
            client.get(request);
        } catch (IOException e) {
            result.addProblem("Can't contact GitHub API", e);
        }

        return result;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
}
