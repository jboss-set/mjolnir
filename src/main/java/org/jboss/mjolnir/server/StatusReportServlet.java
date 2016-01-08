package org.jboss.mjolnir.server;

import org.jboss.mjolnir.server.entities.ApplicationParameterEntity;
import org.jboss.mjolnir.server.entities.GithubOrganizationEntity;
import org.jboss.mjolnir.server.entities.GithubTeamEntity;
import org.jboss.mjolnir.server.entities.UserEntity;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author Martin Stefanko (mstefank@redhat.com)
 */
@WebServlet("/status")
public class StatusReportServlet extends HttpServlet {

    private static final long serialVersionUID = -9076715827695173856L;

    @Inject
    private EntityManagerFactory entityManagerFactory;

    private boolean statusOK;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ServletOutputStream os = response.getOutputStream();
        EntityManager em = null;
        statusOK = true;

        //access the DB
        os.print("Database running: ");
        try {
            em =  entityManagerFactory.createEntityManager();
            os.println("OK");
        }catch (Exception e) {
            processError(os, e);
        }

        if (em != null) {
            //check if all required tables exist

            //application parameters
            os.print("App params: ");
            try {
                final List<ApplicationParameterEntity> app_params =
                        em.createQuery("FROM ApplicationParameterEntity", ApplicationParameterEntity.class).getResultList();
                os.println("OK");
            } catch (Exception e) {
                processError(os, e);
            }

            //github orgs
            os.print("GH organizations: ");
            try {
                final List<GithubOrganizationEntity> gh_orgs =
                        em.createQuery("FROM GithubOrganizationEntity", GithubOrganizationEntity.class).getResultList();
                os.println("OK");
            } catch (Exception e) {
                processError(os, e);
            }

            //github teams
            os.print("GH teams: ");
            try {
                final List<GithubTeamEntity> gh_teams =
                        em.createQuery("FROM GithubTeamEntity", GithubTeamEntity.class).getResultList();
                os.println("OK");
            } catch (Exception e) {
                processError(os, e);
            }

            //users
            os.print("Users: ");
            try {
                List<UserEntity> users = em.createQuery("FROM UserEntity", UserEntity.class).getResultList();
                os.println("OK");
            } catch (Exception e) {
                processError(os, e);
            }

            em.close();
            os.print("DB status: ");
            if (statusOK) {
                os.println("OK");
            } else {
                os.println("Error");
            }
        }

        os.print("Status: ");
        if (statusOK) {
            //everything ok
            os.println("OK");
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            os.println("ERROR");
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        }
    }

    private void processError(ServletOutputStream os, Exception e) throws IOException {
        os.println("Error - " + e.getMessage() + "; please check if the table exists");
        statusOK = false;
    }
}
