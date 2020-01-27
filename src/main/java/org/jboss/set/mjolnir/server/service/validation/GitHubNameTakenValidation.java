package org.jboss.set.mjolnir.server.service.validation;

import org.hibernate.HibernateException;
import org.jboss.set.mjolnir.shared.domain.RegisteredUser;
import org.jboss.set.mjolnir.shared.domain.ValidationResult;
import org.jboss.set.mjolnir.client.exception.ApplicationException;
import org.jboss.set.mjolnir.server.bean.UserRepository;

/**
 * Verifies that given GH name is not used by different Mjolnir user
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class GitHubNameTakenValidation implements Validation<RegisteredUser> {

    private UserRepository userRepository;

    public GitHubNameTakenValidation(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public ValidationResult validate(RegisteredUser entity) {
        try {
            ValidationResult result = new ValidationResult();
            RegisteredUser userByGitHubName = userRepository.getUserByGitHubName(entity.getGithubName());
            if (userByGitHubName != null && !userByGitHubName.equals(entity)) {
                result.addFailure("This GitHub name is already taken by different user.");
            }
            return result;
        } catch (HibernateException e) {
            throw new ApplicationException(e);
        }
    }
}
