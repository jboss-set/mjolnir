package org.jboss.mjolnir.client.domain;

import com.google.gwt.user.client.rpc.IsSerializable;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class EntityUpdateResult<X extends Serializable> implements Serializable, IsSerializable {

    private X updatedEntity;
    private Result result;
    private ValidationResult validationResult;

    public EntityUpdateResult() {
    }

    private EntityUpdateResult(@NotNull Result result, ValidationResult validationResult, X updatedEntity) {
        this.result = result;
        this.validationResult = validationResult;
        this.updatedEntity = updatedEntity;
    }

    public static <Y extends Serializable> EntityUpdateResult<Y> ok(Y entity) {
        return new EntityUpdateResult<Y>(Result.OK, null, entity);
    }

    public static <Y extends Serializable> EntityUpdateResult<Y> validationFailure(ValidationResult validationResult) {
        return new EntityUpdateResult<Y>(Result.VALIDATION_FAILURE, validationResult, null);
    }

    public boolean isOK() {
        return Result.OK.equals(result);
    }

    public X getUpdatedEntity() {
        return updatedEntity;
    }

    public Result getResult() {
        return result;
    }

    public List<String> getValidationMessages() {
        if (validationResult == null) {
            return Collections.emptyList();
        }
        return validationResult.getFailures();
    }

    public enum Result {
        OK,
        VALIDATION_FAILURE
    }

}
