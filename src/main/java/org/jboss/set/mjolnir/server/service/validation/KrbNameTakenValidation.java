package org.jboss.set.mjolnir.server.service.validation;

import org.hibernate.HibernateException;
import org.jboss.set.mjolnir.shared.domain.RegisteredUser;
import org.jboss.set.mjolnir.shared.domain.ValidationResult;
import org.jboss.set.mjolnir.client.exception.ApplicationException;
import org.jboss.set.mjolnir.server.bean.UserRepository;

/**
 * @author Martin Stefanko (mstefank@redhat.com)
 */
public class KrbNameTakenValidation implements Validation<RegisteredUser> {
    private UserRepository userRepository;

    public KrbNameTakenValidation(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public ValidationResult validate(RegisteredUser entity) {
        try {
            ValidationResult result = new ValidationResult();
            RegisteredUser user = userRepository.getUser(entity.getKrbName());
            if (user != null && user.getKrbName() != null) {
                result.addFailure("This Kerberos name is already taken by different user.");
            }
            return result;
        } catch (HibernateException e) {
            throw new ApplicationException(e);
        }
    }
}
