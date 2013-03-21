package org.jboss.mjolnir;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.GitHubResponse;

import java.io.IOException;

/**
 * Main class to be used for this project. In time, we might not need a Main class.
 *
 * @author: navssurtani
 * @since: 0.1
 */
public class Main {

    private static final String BASE_URL = "api.github.com";

    public static void main(String[] args) {
        GitHubRequest request = new GitHubRequest();
        request.setUri(BASE_URL);

        GitHubClient client = new GitHubClient();
        GitHubResponse response = null;
        try {
            response = client.get(request);
            response.getBody()

        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new RuntimeException("There has been an error in executing the request to Github. " +
                    "Your URL might be wrong. " + BASE_URL);
        }
    }

}
