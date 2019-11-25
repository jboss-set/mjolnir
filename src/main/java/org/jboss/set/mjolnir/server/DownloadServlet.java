package org.jboss.set.mjolnir.server;

import org.apache.commons.lang.StringUtils;
import org.jboss.set.mjolnir.server.bean.GitHubSubscriptionBean;
import org.jboss.set.mjolnir.shared.domain.Subscription;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class DownloadServlet extends HttpServlet {

    private static final String CSV_DELIMITER = ";";

    @EJB
    private GitHubSubscriptionBean gitHubSubscriptionBean;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String org = req.getParameter("org");
        String team = req.getParameter("team");

        if (StringUtils.isBlank(org) && StringUtils.isBlank(team)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("'org' or 'team' parameter must be set.");
            resp.setContentType("text/plain");
        }

        resp.setContentType("text/csv");
        resp.addHeader("Content-Disposition", "attachment;filename=gh_org_members.csv");

        List<Subscription> subscriptions;
        if (!StringUtils.isBlank(team)) {
            int teamId = Integer.parseInt(team);
            subscriptions = gitHubSubscriptionBean.getTeamSubscriptions(teamId);
        } else {
            subscriptions = gitHubSubscriptionBean.getOrganizationSubscriptions(org);
        }

        ServletOutputStream os = resp.getOutputStream();
        os.println("KRB name" + CSV_DELIMITER
                + "GH name" + CSV_DELIMITER
                + "Active KRB account" + CSV_DELIMITER
                + "Whitelisted?");

        for (Subscription sub : subscriptions) {
            os.println(emptyOrValue(sub.getKerberosName()) + CSV_DELIMITER
                    + emptyOrValue(sub.getGitHubName()) + CSV_DELIMITER
                    + sub.isActiveKerberosAccount() + CSV_DELIMITER
                    + sub.isWhitelisted());
        }
        os.flush();
        os.close();
    }

    private static String emptyOrValue(String str) {
        return str != null ? str : "";
    }
}
