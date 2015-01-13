package org.jboss.mjolnir.githubclient;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class ExtendedTeamServiceTest {

	private static String USERNAME = "username";
	private static int TEAM_ID = 1;

	private GitHubClient gitHubClient;
	private ExtendedTeamService teamService;

	@Before
	public void setUp() {
		gitHubClient = Mockito.mock(GitHubClient.class);
		teamService = new ExtendedTeamService(gitHubClient);
	}

	@Test
	public void testAddMembership() throws IOException {
		teamService.addMembership(TEAM_ID, USERNAME);
		Mockito.verify(gitHubClient).put("/teams/" + TEAM_ID + "/memberships/" + USERNAME);
		Mockito.verifyNoMoreInteractions(gitHubClient);
	}
}
