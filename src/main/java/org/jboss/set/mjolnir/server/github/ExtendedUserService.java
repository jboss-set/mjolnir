package org.jboss.set.mjolnir.server.github;

import java.io.IOException;

import org.apache.http.HttpStatus;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.UserService;

public class ExtendedUserService extends UserService {

    public ExtendedUserService(GitHubClient client) {
        super(client);
    }

    /**
     * Variant of getUser() method that returns null if user doesn't exist.
     */
    public User getUserIfExists(String login) throws IOException {
        try {
            return super.getUser(login);
        } catch (IOException e) {
            if (e instanceof RequestException) {
                RequestException re = (RequestException) e;
                if (re.getStatus() == HttpStatus.SC_NOT_FOUND) {
                    return null; // user not found
                }
            }
            throw e;
        }
    }
}
