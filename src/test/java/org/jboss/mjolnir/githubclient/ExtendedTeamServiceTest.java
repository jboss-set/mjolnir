package org.jboss.mjolnir.githubclient;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.GitHubResponse;
import org.jboss.mjolnir.server.github.ExtendedTeamService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class ExtendedTeamServiceTest {

    private static String USERNAME = "username";
    private static int TEAM_ID = 1;

    private GitHubClient gitHubClient;
    private ExtendedTeamService teamService;

    @Before
    public void setUp() throws IOException {
        gitHubClient = Mockito.mock(GitHubClient.class);
        Mockito.when(gitHubClient.put(Mockito.anyString(), Mockito.any(), Mockito.any(Type.class)))
                .thenReturn(Collections.singletonMap("state", "active"));
        final GitHubResponse response = new GitHubResponse(null, Collections.singletonMap("state", "active"));
        Mockito.when(gitHubClient.get(Mockito.any(GitHubRequest.class))).thenReturn(response);
        teamService = new ExtendedTeamService(gitHubClient);
    }

    @Test
    public void testAddMembership() throws IOException {
        final String state = teamService.addMembership(TEAM_ID, USERNAME);
        Assert.assertEquals("active", state);
        Mockito.verify(gitHubClient).put("/teams/" + TEAM_ID + "/memberships/" + USERNAME, null, HashMap.class);
        Mockito.verifyNoMoreInteractions(gitHubClient);
    }

    @Test
    public void testRemoveMembership() throws IOException {
        teamService.removeMembership(TEAM_ID, USERNAME);
        Mockito.verify(gitHubClient).delete("/teams/" + TEAM_ID + "/memberships/" + USERNAME);
        Mockito.verifyNoMoreInteractions(gitHubClient);
    }

    @Test
    public void testGetMembership() throws IOException {
        final String state = teamService.getMembership(TEAM_ID, USERNAME);
        Assert.assertEquals("active", state);
        Mockito.verify(gitHubClient).get(Mockito.any(GitHubRequest.class));
        Mockito.verifyNoMoreInteractions(gitHubClient);
    }
}
