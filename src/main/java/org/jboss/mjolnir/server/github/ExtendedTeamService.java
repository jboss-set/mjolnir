package org.jboss.mjolnir.server.github;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.GitHubResponse;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.TeamService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_TEAMS;

/**
 * Extension of TeamService which adds addMembership() method.
 * <p/>
 * (See https://developer.github.com/v3/orgs/teams/#add-team-membership).
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class ExtendedTeamService extends TeamService {

    private static String SEGMENT_MEMBERSHIPS = "/memberships";

    public ExtendedTeamService() {
    }

    public ExtendedTeamService(GitHubClient client) {
        super(client);
    }

    /**
     * Add given user to team with given id
     *
     * @param id
     * @param user
     * @throws java.io.IOException
     */
    public String addMembership(int id, String user) throws IOException {
        if (user == null)
            throw new IllegalArgumentException("User cannot be null");
        if (user.length() == 0)
            throw new IllegalArgumentException("User cannot be empty");

        final StringBuilder uri = new StringBuilder(SEGMENT_TEAMS);
        uri.append('/').append(id);
        uri.append(SEGMENT_MEMBERSHIPS);
        uri.append('/').append(user);
        final Map result = client.put(uri.toString(), null, HashMap.class);
        return (String) result.get("state");
    }

    public String getMembership(int id, String user) throws IOException {
        if (user == null)
            throw new IllegalArgumentException("User cannot be null");
        if (user.length() == 0)
            throw new IllegalArgumentException("User cannot be empty");

        final StringBuilder uri = new StringBuilder(SEGMENT_TEAMS);
        uri.append('/').append(id);
        uri.append(SEGMENT_MEMBERSHIPS);
        uri.append('/').append(user);
        final GitHubRequest request = new GitHubRequest();
        request.setUri(uri);
        request.setType(HashMap.class);
        final GitHubResponse response;
        try {
            response = client.get(request);
        } catch (RequestException e) {
            if (e.getStatus() == HttpURLConnection.HTTP_NOT_FOUND) {
                return MembershipState.NONE;
            }
            throw e;
        }

        final Map map = (Map) response.getBody();
        return (String) map.get("state");
    }

    public void removeMembership(int id, String user) throws IOException {
        if (user == null)
            throw new IllegalArgumentException("User cannot be null");
        if (user.length() == 0)
            throw new IllegalArgumentException("User cannot be empty");

        final StringBuilder uri = new StringBuilder(SEGMENT_TEAMS);
        uri.append('/').append(id);
        uri.append(SEGMENT_MEMBERSHIPS);
        uri.append('/').append(user);

        client.delete(uri.toString());
    }

}
