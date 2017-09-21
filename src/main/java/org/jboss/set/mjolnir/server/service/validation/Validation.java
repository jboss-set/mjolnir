package org.jboss.set.mjolnir.server.service.validation;

import org.jboss.set.mjolnir.shared.domain.ValidationResult;

import java.io.Serializable;

/**
 * Validates entities of given type
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public interface Validation<X extends Serializable> {

    ValidationResult validate(X entity);

}
