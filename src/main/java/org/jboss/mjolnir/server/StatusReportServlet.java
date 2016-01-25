package org.jboss.mjolnir.server;

import java.io.IOException;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.mjolnir.server.service.statuscheck.AbstractStatusCheck;
import org.jboss.mjolnir.server.service.statuscheck.DatabaseStatusCheck;
import org.jboss.mjolnir.server.service.statuscheck.GitHubStatusCheck;
import org.jboss.mjolnir.server.service.statuscheck.KerberosStatusCheck;
import org.jboss.mjolnir.server.service.statuscheck.LdapStatusCheck;
import org.jboss.mjolnir.server.service.statuscheck.StatusCheckResult;

/**
 * @author Martin Stefanko (mstefank@redhat.com)
 */
@WebServlet("/status")
public class StatusReportServlet extends HttpServlet {

    @Inject
    private EntityManagerFactory entityManagerFactory;

    @Inject
    private DatabaseStatusCheck databaseStatusCheck;

    @Inject
    private GitHubStatusCheck gitHubStatusCheck;

    @Inject
    private KerberosStatusCheck kerberosStatusCheck;

    @Inject
    private LdapStatusCheck ldapStatusCheck;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ServletOutputStream os = response.getOutputStream();
        boolean statusOK = true;

        AbstractStatusCheck[] checks = new AbstractStatusCheck[] {
                databaseStatusCheck, gitHubStatusCheck, kerberosStatusCheck, ldapStatusCheck
        };

        for (AbstractStatusCheck check: checks) {
            StatusCheckResult result = check.checkStatus();
            os.println(check.getTitle() + ": " + result.toString());
            statusOK = statusOK && result.isSuccess();
        }

        response.setStatus(statusOK ? HttpServletResponse.SC_OK : HttpServletResponse.SC_SERVICE_UNAVAILABLE);
    }
}
