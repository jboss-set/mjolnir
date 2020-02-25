package org.jboss.set.mjolnir.server.service.statuscheck;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class GitHubStatusCheckTest {

    @Test
    public void testHostDoesntExist() {
        GitHubStatusCheck gitHubStatusCheck = new GitHubStatusCheck();
        gitHubStatusCheck.setHostName("127.0.0.3"); // non existing host
        StatusCheckResult result = gitHubStatusCheck.checkStatus();
        Assert.assertFalse(result.isSuccess());
    }
}
