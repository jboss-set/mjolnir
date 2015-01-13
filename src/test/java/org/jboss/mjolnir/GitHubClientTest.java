package org.jboss.mjolnir;


import org.eclipse.egit.github.core.Team;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.jboss.mjolnir.githubclient.ExtendedTeamService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * Sample test class illustrating the use of GitHubClient.
 *
 * (Not to be run during build process.)
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
@Ignore
public class GitHubClientTest {

	private static String TOKEN = "408af3afceec32628057e6cc6c8eda738c4a8951";
	private static int TEAM_ID = 1223195;
	private static String ORG_NAME = "thofmantestorg";
	private static String USERNAME = "TomasHofman";

	private GitHubClient gitHubClient = new GitHubClient();
	private ExtendedTeamService teamService = new ExtendedTeamService(gitHubClient);

	@Before
	public void setUp() {
		gitHubClient.setOAuth2Token(TOKEN);
	}

	@Test
	public void listTeams() throws IOException {
		final List<Team> teams = teamService.getTeams(ORG_NAME);
		System.out.println(teams);
	}

	@Test
	public void addTeamMember() throws IOException {
		teamService.addMember(TEAM_ID, USERNAME);
	}

	@Test
	public void addTeamMembersip() throws IOException {
		teamService.addMembership(TEAM_ID, USERNAME);
	}

	@Test
	public void getMembers() throws IOException {
		List<User> members = teamService.getMembers(TEAM_ID);
		System.out.println(members);
	}

}
