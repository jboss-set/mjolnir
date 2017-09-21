package org.jboss.set.mjolnir.server.service.validation;

import org.jboss.set.mjolnir.shared.domain.ValidationResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Groups Validations
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class Validator<X extends Serializable> implements Validation<X> {

    private List<Validation<X>> validations = new ArrayList<Validation<X>>();

    public void addValidation(Validation<X> validation) {
        if(!validations.contains(validation)) {
            validations.add(validation);
        }
    }

    public void removeValidation(Validation<X> validation) {
        validations.remove(validation);
    }

    @Override
    public ValidationResult validate(X entity) {
        ValidationResult result = new ValidationResult();

        for (Validation<X> validation: validations) {
            ValidationResult validationResult = validation.validate(entity);
            if (!validationResult.isOK()) {
                result.addFailures(validationResult.getFailures());
            }
        }

        return result;
    }
}
