package org.jboss.mjolnir.server.service.validation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jboss.mjolnir.client.domain.ValidationResult;

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
