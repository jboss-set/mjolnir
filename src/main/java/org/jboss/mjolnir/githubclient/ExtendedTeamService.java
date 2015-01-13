package org.jboss.mjolnir.githubclient;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.TeamService;

import java.io.IOException;

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
	public void addMembership(int id, String user) throws IOException {
		if (user == null)
			throw new IllegalArgumentException("User cannot be null"); //$NON-NLS-1$
		if (user.length() == 0)
			throw new IllegalArgumentException("User cannot be empty"); //$NON-NLS-1$

		StringBuilder uri = new StringBuilder(SEGMENT_TEAMS);
		uri.append('/').append(id);
		uri.append(SEGMENT_MEMBERSHIPS);
		uri.append('/').append(user);
		client.put(uri.toString());
	}

}
