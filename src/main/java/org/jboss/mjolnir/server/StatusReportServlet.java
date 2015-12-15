package org.jboss.mjolnir.server;

import org.jboss.mjolnir.server.bean.ApplicationParameters;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintStream;

/**
 * @author Martin Stefanko (mstefank@redhat.com)
 */
@WebServlet("/status")
public class StatusReportServlet extends HttpServlet {

    private static final long serialVersionUID = -9076715827695173856L;

    @EJB
    private ApplicationParameters applicationParameters;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {
            //access the DB
            String token = applicationParameters.getMandatoryParameter(ApplicationParameters.GITHUB_TOKEN_KEY);
        } catch (Exception e) {
            response.getOutputStream().println("Cannot connect to the database.");
            e.printStackTrace(new PrintStream(response.getOutputStream()));
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            return;
        }

        //everything ok
        response.getOutputStream().println("OK");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
