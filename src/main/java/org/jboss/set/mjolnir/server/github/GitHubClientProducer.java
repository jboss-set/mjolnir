package org.jboss.set.mjolnir.server.github;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.jboss.set.mjolnir.server.bean.ApplicationParameters;

@SuppressWarnings("unused")
public class GitHubClientProducer {

    @ApplicationScoped
    @Produces
    public GitHubClient createGitHubClient(ApplicationParameters applicationParameters) {
        String token = applicationParameters.getMandatoryParameter(ApplicationParameters.GITHUB_TOKEN_KEY);
        GitHubClient client = new GitHubClient();
        client.setOAuth2Token(token);
        return client;
    }
}
