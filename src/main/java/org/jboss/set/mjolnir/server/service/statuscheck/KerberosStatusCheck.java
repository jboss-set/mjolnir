package org.jboss.set.mjolnir.server.service.statuscheck;

import java.io.IOException;
import java.net.Socket;

import javax.ejb.EJB;
import javax.ejb.Singleton;

import org.jboss.set.mjolnir.server.bean.ApplicationParameters;

/**
 * Checks that Kerberos KDC is reachable.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
@Singleton
public class KerberosStatusCheck extends AbstractStatusCheck {

    private static final String TITLE = "Kerberos";
    private static final String DEFAULT_KDC = "kerberos.corp.redhat.com";

    @EJB
    private ApplicationParameters applicationParameters;

    public KerberosStatusCheck() {
        super(TITLE);
    }

    @Override
    protected StatusCheckResult doCheckStatus() throws Exception {
        StatusCheckResult result = new StatusCheckResult();

        String kdc = applicationParameters.getParameter(ApplicationParameters.KRB5_KDC_KEY);
        if (kdc == null) {
            kdc = DEFAULT_KDC;
        }

        Socket socket = null;
        try {
            socket = new Socket(kdc, 88);
        } catch (IOException e) {
            result.addProblem(String.format("Can't connect to KDC %s", kdc), e);
        } finally {
            if (socket != null) {
                socket.close();
            }
        }

        return result;
    }

    void setApplicationParameters(ApplicationParameters applicationParameters) {
        this.applicationParameters = applicationParameters;
    }
}
