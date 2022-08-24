package org.jboss.set.mjolnir.server.service.validation;

import org.jboss.set.mjolnir.shared.domain.RegisteredUser;
import org.jboss.set.mjolnir.shared.domain.ValidationResult;

/**
 * Validates that when a user has been marked as whitelisted, responsible person has also been set.
 */
public class ResponsiblePersonAddedValidation implements Validation<RegisteredUser> {

    @Override
    public ValidationResult validate(RegisteredUser entity) {
        ValidationResult result = new ValidationResult();
        if (!entity.isWhitelisted() || (entity.isWhitelisted() && !entity.getResponsiblePerson().isEmpty())) {
            return result; // OK
        } else if  (entity.isWhitelisted() && entity.getResponsiblePerson().isEmpty()) {
            result.addFailure("Whitelisted person must have responsible person assigned.");
        }
        return result;
    }
}
