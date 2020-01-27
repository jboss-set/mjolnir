package org.jboss.set.mjolnir.shared.domain;

import java.io.Serializable;

/**
 * Domain object representing user subscribed to GitHub organization.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class Subscription implements Serializable {

    private String gitHubName;
    private RegisteredUser registeredUser;
    private boolean activeKerberosAccount;

    public String getGitHubName() {
        return gitHubName;
    }

    public void setGitHubName(String gitHubName) {
        this.gitHubName = gitHubName;
    }

    public RegisteredUser getRegisteredUser() {
        return registeredUser;
    }

    public void setRegisteredUser(RegisteredUser registeredUser) {
        this.registeredUser = registeredUser;
    }

    public boolean isActiveKerberosAccount() {
        return activeKerberosAccount;
    }

    public void setActiveKerberosAccount(boolean hasActiveKerberosAccount) {
        this.activeKerberosAccount = hasActiveKerberosAccount;
    }

    public String getKerberosName() {
        return registeredUser != null ? registeredUser.getKrbName() : null;
    }

    public boolean isWhitelisted() {
        return registeredUser != null && registeredUser.isWhitelisted();
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "gitHubName='" + gitHubName + '\'' +
                ", kerberosUser=" + registeredUser +
                ", activeKerberosAccount=" + activeKerberosAccount +
                '}';
    }

    /**
     * User can have a null name, so two entities are equal only when they have the same github name.
     *
     * @param o object to compare to
     * @return are equal?
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subscription)) return false;

        Subscription that = (Subscription) o;

        return gitHubName.equals(that.gitHubName);

    }

    @Override
    public int hashCode() {
        int result = gitHubName != null ? gitHubName.hashCode() : 0;
        result = 31 * result + (registeredUser != null ? registeredUser.hashCode() : 0);
        return result;
    }
}
