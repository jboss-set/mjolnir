package org.jboss.set.mjolnir.server.service.validation;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.jboss.set.mjolnir.shared.domain.RegisteredUser;
import org.jboss.set.mjolnir.shared.domain.ValidationResult;
import org.jboss.set.mjolnir.client.exception.ApplicationException;
import org.jboss.set.mjolnir.server.bean.UserRepository;

/**
 * Validates that Kerberos (LDAP) name is not used by different registered user.
 *
 * @author Martin Stefanko (mstefank@redhat.com)
 */
public class KrbNameIsUniqueValidation implements Validation<RegisteredUser> {

    private static final String VALIDATION_MESSAGE = "This Kerberos name is already taken by different user.";

    private final UserRepository userRepository;

    public KrbNameIsUniqueValidation(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public ValidationResult validate(RegisteredUser userToValidate) {
        try {
            ValidationResult result = new ValidationResult();
            if (StringUtils.isEmpty(userToValidate.getKrbName())) {
                return result; // OK
            }

            RegisteredUser existingEntity = userRepository.getUser(userToValidate.getKrbName());
            if (userToValidate.getId() == null && existingEntity != null) {
                // registering new user but the LDAP name is already used
                result.addFailure(VALIDATION_MESSAGE);
            } else if (userToValidate.getId() != null && existingEntity != null && !userToValidate.getId().equals(existingEntity.getId())) {
                // modifying existing user but a different user uses this LDAP name
                result.addFailure(VALIDATION_MESSAGE);
            }
            return result;
        } catch (HibernateException e) {
            throw new ApplicationException(e);
        }
    }
}
