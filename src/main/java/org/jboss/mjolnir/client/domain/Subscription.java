package org.jboss.mjolnir.client.domain;

import org.jboss.mjolnir.authentication.KerberosUser;

import java.io.Serializable;

/**
 * Domain object representing user subscribed to GitHub organization.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class Subscription implements Serializable {

    private String gitHubName;
    private KerberosUser kerberosUser;
    private boolean activeKerberosAccount;

    public String getGitHubName() {
        return gitHubName;
    }

    public void setGitHubName(String gitHubName) {
        this.gitHubName = gitHubName;
    }

    public KerberosUser getKerberosUser() {
        return kerberosUser;
    }

    public void setKerberosUser(KerberosUser kerberosUser) {
        this.kerberosUser = kerberosUser;
    }

    public boolean isActiveKerberosAccount() {
        return activeKerberosAccount;
    }

    public void setActiveKerberosAccount(boolean hasActiveKerberosAccount) {
        this.activeKerberosAccount = hasActiveKerberosAccount;
    }

    public String getKerberosName() {
        if (kerberosUser != null) {
            return kerberosUser.getName();
        }
        return null;
    }

}
