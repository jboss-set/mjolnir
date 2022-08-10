package org.jboss.set.mjolnir.server.service.validation;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.egit.github.core.User;
import org.jboss.set.mjolnir.server.github.ExtendedUserService;
import org.jboss.set.mjolnir.shared.domain.RegisteredUser;
import org.jboss.set.mjolnir.shared.domain.ValidationResult;
import org.jboss.set.mjolnir.client.exception.ApplicationException;

import java.io.IOException;

/**
 * Verifies that given username is registered on GitHub
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class GitHubNameExistsValidation implements Validation<RegisteredUser> {

    private final ExtendedUserService userService;

    public GitHubNameExistsValidation(ExtendedUserService userService) {
        this.userService = userService;
    }

    @Override
    public ValidationResult validate(RegisteredUser entity) {
        ValidationResult result = new ValidationResult();
        try {
            if (StringUtils.isNotBlank(entity.getGitHubName())) {
                User user = userService.getUserIfExists(entity.getGitHubName());
                if (user == null) {
                    result.addFailure("Specified GitHub name is not registered on GitHub.");
                }
            }
        } catch (IOException e) {
            throw new ApplicationException("GitHub API call failure", e);
        }
        return result;
    }
}
