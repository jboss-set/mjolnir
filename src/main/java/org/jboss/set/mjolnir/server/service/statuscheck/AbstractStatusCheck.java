package org.jboss.set.mjolnir.server.service.statuscheck;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public abstract class AbstractStatusCheck {

    private String title;

    /**
     * @param title check title - will be printed on status page in a form "Title: RESULT"
     */
    public AbstractStatusCheck(String title) {
        this.title = title;
    }

    public StatusCheckResult checkStatus() {
        try {
            return doCheckStatus();
        } catch (Throwable t) {
            StatusCheckResult result = new StatusCheckResult();
            result.addProblem("Status check failed", t);
            return result;
        }
    }

    public String getTitle() {
        return title;
    }

    /**
     * Performs actual check - to be implemented by implementing classes
     *
     * If the check can be successfully completed, method should return StatusCheckResult containing eventual problems
     * descriptions. Exception can be thrown if it's not possible to perform the check for some reason.
     *
     * @return StatusCheckResult containing eventual problems
     * @throws Exception
     */
    protected abstract StatusCheckResult doCheckStatus() throws Exception;
}
