package org.jboss.set.mjolnir.server.service.validation;

import org.apache.commons.lang3.StringUtils;
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
public class GitHubNameRegisteredValidation implements Validation<RegisteredUser> {

    private final UserRepository userRepository;

    public GitHubNameRegisteredValidation(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public ValidationResult validate(RegisteredUser entity) {
        try {
            ValidationResult result = new ValidationResult();
            if (!StringUtils.isEmpty(entity.getGitHubName())) {
                RegisteredUser existingEntity = userRepository.getUserByGitHubName(entity.getGitHubName());
                if (existingEntity != null && !existingEntity.getId().equals(entity.getId())) {
                    result.addFailure("This GitHub name has already been registered.");
                }
            }
            return result;
        } catch (HibernateException e) {
            throw new ApplicationException(e);
        }
    }
}
