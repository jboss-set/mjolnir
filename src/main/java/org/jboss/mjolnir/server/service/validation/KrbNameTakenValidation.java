package org.jboss.mjolnir.server.service.validation;

import org.hibernate.HibernateException;
import org.jboss.mjolnir.shared.domain.KerberosUser;
import org.jboss.mjolnir.shared.domain.ValidationResult;
import org.jboss.mjolnir.client.exception.ApplicationException;
import org.jboss.mjolnir.server.bean.UserRepository;

/**
 * @author Martin Stefanko (mstefank@redhat.com)
 */
public class KrbNameTakenValidation implements Validation<KerberosUser> {
    private UserRepository userRepository;

    public KrbNameTakenValidation(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public ValidationResult validate(KerberosUser entity) {
        try {
            ValidationResult result = new ValidationResult();
            KerberosUser user = userRepository.getUser(entity.getName());
            if (user != null && user.getName() != null) {
                result.addFailure("This Kerberos name is already taken by different user.");
            }
            return result;
        } catch (HibernateException e) {
            throw new ApplicationException(e);
        }
    }
}
