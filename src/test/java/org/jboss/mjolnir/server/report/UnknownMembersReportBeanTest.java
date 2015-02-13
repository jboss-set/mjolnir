package org.jboss.mjolnir.server.report;

import org.jboss.mjolnir.authentication.GithubOrganization;
import org.jboss.mjolnir.authentication.KerberosUser;
import org.jboss.mjolnir.client.domain.Subscription;
import org.jboss.mjolnir.client.domain.SubscriptionSummary;
import org.jboss.mjolnir.server.bean.GitHubSubscriptionBean;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class UnknownMembersReportBeanTest {

    private static final String UNKNOWN_NAME1 = "Tomas Hofman";
    private static final String UNKNOWN_NAME2 = "John Smith";
    private static final String PAST_EMPLOYEE_NAME = "John Deer";
    private static final String CURRENT_EMPLOYEE_NAME = "Robin Hood";

    private UnknownMembersReportBean reportBean;

    @Before
    public void setUp() {
        SubscriptionSummary summary = new SubscriptionSummary();
        summary.setOrganization(new GithubOrganization("Some Org"));
        summary.getSubscriptions().add(createSubscription(UNKNOWN_NAME1, null, false));
        summary.getSubscriptions().add(createSubscription(UNKNOWN_NAME2, null, false));
        summary.getSubscriptions().add(createSubscription(PAST_EMPLOYEE_NAME, "jdeer", false));
        summary.getSubscriptions().add(createSubscription(CURRENT_EMPLOYEE_NAME, "rhood", true));

        final GitHubSubscriptionBean gitHubSubscriptionBean = Mockito.mock(GitHubSubscriptionBean.class);
        Mockito.when(gitHubSubscriptionBean.getOrganizationMembers()).thenReturn(Arrays.asList(summary));

        reportBean = new UnknownMembersReportBean();
        reportBean.setGitHubSubscriptionBean(gitHubSubscriptionBean);
    }

    @Test
    public void generateReportTest() {
        /*final Report<List<SubscriptionSummary>> report = reportBean.generateReport();
        final String reportContent = report.getContent();

        System.out.println(reportContent);

        Assert.assertTrue(reportContent.contains(UNKNOWN_NAME1));
        Assert.assertTrue(reportContent.contains(UNKNOWN_NAME2));
        Assert.assertTrue(reportContent.contains(PAST_EMPLOYEE_NAME));
        Assert.assertFalse(reportContent.contains(CURRENT_EMPLOYEE_NAME));*/
    }

    private Subscription createSubscription(String gitHubName, String krbName, boolean hasKrbAccount) {
        final Subscription subscription = new Subscription();
        subscription.setGitHubName(gitHubName);

        if (krbName != null) {
            subscription.setKerberosUser(new KerberosUser());
            subscription.getKerberosUser().setName(krbName);
            subscription.setActiveKerberosAccount(hasKrbAccount);
        }

        return subscription;
    }
}
