package org.jboss.mjolnir.server.github;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.GitHubResponse;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.TeamService;
import org.jboss.mjolnir.shared.domain.MembershipStates;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_TEAMS;

/**
 * Extension of TeamService which adds addMembership() method.
 *
 * (See https://developer.github.com/v3/orgs/teams/#add-team-membership).
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class ExtendedTeamService extends TeamService {

    private static String SEGMENT_MEMBERSHIPS = "/memberships";

    public ExtendedTeamService(GitHubClient client) {
        super(client);
    }

    /**
     * Add given user to team with given id
     *
     * @param id team id
     * @param user user name
     * @throws java.io.IOException
     */
    public String addMembership(int id, String user) throws IOException {
        if (user == null)
            throw new IllegalArgumentException("User cannot be null");
        if (user.length() == 0)
            throw new IllegalArgumentException("User cannot be empty");

        final Map result = client.put(SEGMENT_TEAMS + '/' + id + SEGMENT_MEMBERSHIPS + '/' + user, null, HashMap.class);
        return (String) result.get("state");
    }

    /**
     *
     * @param id team id
     * @param user user name
     * @return membership state
     * @throws IOException
     */
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
                return MembershipStates.NONE;
            }
            throw e;
        }

        final Map map = (Map) response.getBody();
        return (String) map.get("state");
    }

    /**
     *
     * @param id team id
     * @param user user name
     * @throws IOException
     */
    public void removeMembership(int id, String user) throws IOException {
        if (user == null)
            throw new IllegalArgumentException("User cannot be null");
        if (user.length() == 0)
            throw new IllegalArgumentException("User cannot be empty");

        client.delete(SEGMENT_TEAMS + '/' + id + SEGMENT_MEMBERSHIPS + '/' + user);
    }



}
