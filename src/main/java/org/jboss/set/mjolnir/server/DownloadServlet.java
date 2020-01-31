package org.jboss.set.mjolnir.server;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.jboss.set.mjolnir.server.bean.GitHubSubscriptionBean;
import org.jboss.set.mjolnir.shared.domain.Subscription;

import javax.ejb.EJB;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class DownloadServlet extends HttpServlet {

    private static final String CSV_DELIMITER = ";";

    @EJB
    private GitHubSubscriptionBean gitHubSubscriptionBean;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String org = req.getParameter("org");
        String team = req.getParameter("team");
        String ghName = req.getParameter("ghName");
        String krbName = req.getParameter("krbName");
        String krbAccount = req.getParameter("krbAccount");
        Boolean krbAccountBoolean = isBlank(krbAccount) ? null : Boolean.parseBoolean(krbAccount);
        String whitelisted = req.getParameter("whitelisted");
        Boolean whitelistedBoolean = isBlank(whitelisted) ? null : Boolean.parseBoolean(whitelisted);

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

        CollectionUtils.filter(subscriptions, new FilteringPredicate(ghName, krbName, krbAccountBoolean,
                whitelistedBoolean));

        ServletOutputStream os = resp.getOutputStream();
        os.println("KRB name" + CSV_DELIMITER
                + "GH name" + CSV_DELIMITER
                + "Note" + CSV_DELIMITER
                + "Active KRB account" + CSV_DELIMITER
                + "Whitelisted?");

        for (Subscription sub : subscriptions) {
            os.println(emptyOrValue(sub.getKerberosName()) + CSV_DELIMITER
                    + emptyOrValue(sub.getGitHubName()) + CSV_DELIMITER
                    + emptyOrValue(sub.getRegisteredUser() != null ? sub.getRegisteredUser().getNote() : "") + CSV_DELIMITER
                    + sub.isActiveKerberosAccount() + CSV_DELIMITER
                    + sub.isWhitelisted());
        }
        os.flush();
        os.close();
    }

    private static String emptyOrValue(String str) {
        return str != null ? str : "";
    }

    private static class FilteringPredicate implements Predicate {

        private String ghName;
        private String krbName;
        private Boolean krbAccount;
        private Boolean whitelisted;

        FilteringPredicate(String ghName, String krbName, Boolean krbAccount, Boolean whitelisted) {
            this.ghName = ghName;
            this.krbName = krbName;
            this.krbAccount = krbAccount;
            this.whitelisted = whitelisted;
        }

        @Override
        public boolean evaluate(Object o) {
            Subscription subscription = (Subscription) o;
            if (!isBlank(ghName)) {
                if (isBlank(subscription.getGitHubName())
                        || !subscription.getGitHubName().toLowerCase().contains(ghName.toLowerCase())) {
                    return false;
                }
            }
            if (!isBlank(krbName)) {
                if (isBlank(subscription.getKerberosName())
                        || !subscription.getKerberosName().toLowerCase().contains(krbName.toLowerCase())) {
                    return false;
                }
            }
            if (krbAccount != null && subscription.isActiveKerberosAccount() != krbAccount) {
                return false;
            }
            if (whitelisted != null && subscription.isWhitelisted() != whitelisted) {
                return false;
            }
            return true;
        }
    }
}
