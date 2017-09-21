package org.jboss.set.mjolnir.shared.domain;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents validation result.
 *
 * Result is OK, unless failures are added.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class ValidationResult implements Serializable, IsSerializable {

    public static final ValidationResult OK = new ValidationResult();

    private List<String> failures;
    private Result result;

    public ValidationResult() {
        failures = new ArrayList<String>();
        result = Result.OK;
    }

    public void addFailure(String message) {
        result = Result.FAILURE;
        failures.add(message);
    }

    public void addFailures(List<String> messages) {
        result = Result.FAILURE;
        failures.addAll(messages);
    }

    public boolean isOK() {
        return Result.OK.equals(result);
    }

    public List<String> getFailures() {
        return failures;
    }

    public Result getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "ValidationResult{" +
                "failures=" + failures +
                ", result=" + result +
                '}';
    }

    enum Result {
        OK, FAILURE
    }

}
