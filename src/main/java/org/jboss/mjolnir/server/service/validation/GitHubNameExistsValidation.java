package org.jboss.mjolnir.server.service.validation;

import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.UserService;
import org.jboss.mjolnir.shared.domain.KerberosUser;
import org.jboss.mjolnir.shared.domain.ValidationResult;
import org.jboss.mjolnir.client.exception.ApplicationException;

import java.io.IOException;

/**
 * Verifies that given username is registered on GitHub
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class GitHubNameExistsValidation implements Validation<KerberosUser> {

    private static final String CALL_FAILURE = "GitHub call failed.";

    private UserService userService;

    public GitHubNameExistsValidation(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ValidationResult validate(KerberosUser entity) {
        ValidationResult result = new ValidationResult();
        try {
            if(entity.getGithubName().equals("")) {
                result.addFailure("GitHub name is required.");
            } else {
                userService.getUser(entity.getGithubName()); // throws RequestException 404 if user doesn't exist
            }
        } catch (RequestException e) {
            if (e.getStatus() == 404) {
                result.addFailure("Specified GitHub name is not registered on GitHub.");
            }
            else if (e.getStatus() == 401) {
                throw new ApplicationException("Specified Github token is invalid. Refer to README.md file for more information.", e);
            }
            else {
                throw new ApplicationException(CALL_FAILURE, e);
            }
        } catch (IOException e) {
            throw new ApplicationException(CALL_FAILURE, e);
        }
        return result;
    }
}
