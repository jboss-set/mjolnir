package org.jboss.set.mjolnir.server.report;

import org.apache.commons.lang3.StringUtils;
import org.jboss.set.mjolnir.shared.domain.Subscription;
import org.jboss.set.mjolnir.shared.domain.SubscriptionSummary;
import org.jboss.set.mjolnir.server.bean.GitHubSubscriptionBean;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Retrieves list of users subscribed to some GitHub organization, that are either not registered in Mjolnir
 * or doesn't have active KRB account.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
@Singleton
public class UnknownMembersReportBean extends AbstractReportBean<List<SubscriptionSummary>> {

    private static final Logger logger = Logger.getLogger("");

    public static final String UNSUBSCRIBE_USERS_ACTION_NAME = "Unsubscribe unknown users";

    private static final String REPORT_NAME = "Unknown GitHub Subscribers";
    private static final String[] TABLE_HEADERS = {"GitHub Name", "Registered As"};

    @EJB
    private GitHubSubscriptionBean gitHubSubscriptionBean;

    public UnknownMembersReportBean() {
        super(REPORT_NAME);

        addReportAction(UNSUBSCRIBE_USERS_ACTION_NAME, new UnsubscribeUnknownUsersAction()); // a bit too dangerous for now, someone could click it
    }

    @Override
    protected List<SubscriptionSummary> loadData() {
        final List<SubscriptionSummary> organizationMembers = gitHubSubscriptionBean.getOrganizationMembers();

        // remove all that have active krb account or are whitelisted
        for (SubscriptionSummary summary : organizationMembers) {
            List<Subscription> unknownUsers = new ArrayList<Subscription>(summary.getSubscriptions());
            for (Subscription subscription : summary.getSubscriptions()) {
                if (subscription.isActiveKerberosAccount() || subscription.isWhitelisted()) {
                    unknownUsers.remove(subscription);
                }
            }
            summary.getSubscriptions().clear();
            summary.getSubscriptions().addAll(unknownUsers);
        }

        // what remains are unknown members
        return organizationMembers;
    }

    @Override
    protected String createReportContent(List<SubscriptionSummary> data) {
        final StringBuilder sb = new StringBuilder();

        for (SubscriptionSummary summary : data) {
            sb.append("Following users are subscribed to GitHub organization ")
                    .append(summary.getOrganization().getName())
                    .append(" but are either not registered in Mjolnir, or do not have valid Kerberos account:\n\n");

            // calculate column sizes
            int gitHubNameMaxLen = TABLE_HEADERS[0].length();
            int registeredNameMaxLen = TABLE_HEADERS[1].length();
            for (Subscription subscription : summary.getSubscriptions()) {
                if (subscription.getGitHubName().length() > gitHubNameMaxLen) {
                    gitHubNameMaxLen = subscription.getGitHubName().length();
                }
                if (subscription.getKerberosUser() != null && subscription.getKerberosName() != null
                        && subscription.getKerberosName().length() > registeredNameMaxLen) {
                    registeredNameMaxLen = subscription.getKerberosName().length();
                }
            }

            // print table headers
            sb.append(String.format("%" + gitHubNameMaxLen + "s", TABLE_HEADERS[0]))
                    .append(" | ")
                    .append(String.format("%" + registeredNameMaxLen + "s", TABLE_HEADERS[1]))
                    .append("\n")
                    .append(StringUtils.repeat('=', gitHubNameMaxLen + registeredNameMaxLen + 3))
                    .append("\n");


            // print table
            for (Subscription subscription : summary.getSubscriptions()) {
                final String krbName = subscription.getKerberosName() != null ? subscription.getKerberosName() : "-";
                sb.append(String.format("%" + gitHubNameMaxLen + "s", subscription.getGitHubName()))
                        .append(" | ")
                        .append(String.format("%" + registeredNameMaxLen + "s", krbName))
                        .append("\n");
            }

            sb.append("\n\n\n");
        }

        return sb.toString();
    }

    public void setGitHubSubscriptionBean(GitHubSubscriptionBean gitHubSubscriptionBean) {
        this.gitHubSubscriptionBean = gitHubSubscriptionBean;
    }


    /**
     * Report action that unsubscribes reported unknown users from GitHub organizations.
     */
    private class UnsubscribeUnknownUsersAction implements ReportAction<List<SubscriptionSummary>> {

        @Override
        public void doAction(List<SubscriptionSummary> data) {
            for (SubscriptionSummary summary: data) {
                for (Subscription subscription: summary.getSubscriptions()) {
                    logger.log(Level.WARNING, "Removing user " + subscription.getGitHubName() + " from organization " + summary.getOrganization().getName());
                    gitHubSubscriptionBean.unsubscribeUser(summary.getOrganization().getName(), subscription.getGitHubName());
                }
            }
        }
    }
}
