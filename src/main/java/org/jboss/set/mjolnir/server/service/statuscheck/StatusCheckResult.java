package org.jboss.set.mjolnir.server.service.statuscheck;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public final class StatusCheckResult {

    private boolean success;
    private List<Problem> problems = new ArrayList<>();

    public StatusCheckResult() {
        success = true;
    }

    public void addProblem(String message, Throwable exception) {
        success = false;
        problems.add(new Problem(message, exception));
    }

    public void addProblem(String message) {
        addProblem(message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    @Override
    public String toString() {
        if (success) {
            return "OK";
        } else {
            StringBuilder sb = new StringBuilder("ERROR");
            for (Problem problem : problems) {
                sb.append("\n  ")
                        .append(problem.message);
                if (problem.exception != null) {
                    sb.append(": ").append(ExceptionUtils.getStackTrace(problem.exception));
                }
            }
            return sb.toString();
        }
    }

    private static class Problem {
        private String message;
        private Throwable exception;

        Problem(String message, Throwable exception) {
            this.message = message;
            this.exception = exception;
        }

    }
}
