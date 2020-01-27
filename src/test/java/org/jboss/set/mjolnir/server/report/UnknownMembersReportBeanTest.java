package org.jboss.set.mjolnir.server.report;

import org.jboss.set.mjolnir.shared.domain.GithubOrganization;
import org.jboss.set.mjolnir.shared.domain.RegisteredUser;
import org.jboss.set.mjolnir.shared.domain.Report;
import org.jboss.set.mjolnir.shared.domain.Subscription;
import org.jboss.set.mjolnir.shared.domain.SubscriptionSummary;
import org.jboss.set.mjolnir.server.bean.GitHubSubscriptionBean;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class UnknownMembersReportBeanTest {

    private static final String UNKNOWN_NAME1 = "Tomas Hofman";
    private static final String UNKNOWN_NAME2 = "John Smith";
    private static final String PAST_EMPLOYEE_NAME = "John Deer";
    private static final String CURRENT_EMPLOYEE_NAME = "Robin Hood";
    private static final String ORGANIZATION_NAME = "Some org";

    private UnknownMembersReportBean reportBean;
    private GitHubSubscriptionBean gitHubSubscriptionBean;

    @Before
    public void setUp() {
        SubscriptionSummary summary = new SubscriptionSummary();
        summary.setOrganization(new GithubOrganization(ORGANIZATION_NAME));
        summary.getSubscriptions().add(createSubscription(UNKNOWN_NAME1, null, false)); // totally unknown members
        summary.getSubscriptions().add(createSubscription(UNKNOWN_NAME2, null, false));
        summary.getSubscriptions().add(createSubscription(PAST_EMPLOYEE_NAME, "jdeer", false)); // past employee - is registered, but doesn't have KRB account
        summary.getSubscriptions().add(createSubscription(CURRENT_EMPLOYEE_NAME, "rhood", true)); // current employee - registered and valid krb account - should not appear in report result

        gitHubSubscriptionBean = Mockito.mock(GitHubSubscriptionBean.class);
        Mockito.when(gitHubSubscriptionBean.getOrganizationMembers()).thenReturn(Arrays.asList(summary));

        reportBean = new UnknownMembersReportBean();
        reportBean.setGitHubSubscriptionBean(gitHubSubscriptionBean);
    }

    @Test
    public void generateReportTest() {
        final Report<List<SubscriptionSummary>> report = reportBean.generateReport();

        // check report data
        final List<SubscriptionSummary> data = report.getData();
        Assert.assertEquals(1, data.size());
        Assert.assertEquals(3, data.get(0).getSubscriptions().size());

        // check report content (which is free text)
        final String reportContent = report.getContent();

        System.out.println(reportContent);

        Assert.assertTrue(reportContent.contains(UNKNOWN_NAME1));
        Assert.assertTrue(reportContent.contains(UNKNOWN_NAME2));
        Assert.assertTrue(reportContent.contains(PAST_EMPLOYEE_NAME));
        Assert.assertFalse(reportContent.contains(CURRENT_EMPLOYEE_NAME));
    }

    @Test
    public void unsubscribeUsersActionTest() {
        // get report data - reusing prepared data from previous test
        final Report<List<SubscriptionSummary>> report = reportBean.generateReport();
        final List<SubscriptionSummary> data = report.getData();
        Assert.assertEquals(1, data.size());
        Assert.assertEquals(3, data.get(0).getSubscriptions().size());

        // run report action
        reportBean.performReportAction(UnknownMembersReportBean.UNSUBSCRIBE_USERS_ACTION_NAME, data);

        // check service calls
        Mockito.verify(gitHubSubscriptionBean).getOrganizationMembers();
        Mockito.verify(gitHubSubscriptionBean).unsubscribeUser(ORGANIZATION_NAME, UNKNOWN_NAME1);
        Mockito.verify(gitHubSubscriptionBean).unsubscribeUser(ORGANIZATION_NAME, UNKNOWN_NAME2);
        Mockito.verify(gitHubSubscriptionBean).unsubscribeUser(ORGANIZATION_NAME, PAST_EMPLOYEE_NAME);
        Mockito.verifyNoMoreInteractions(gitHubSubscriptionBean);
    }

    private Subscription createSubscription(String gitHubName, String krbName, boolean hasKrbAccount) {
        final Subscription subscription = new Subscription();
        subscription.setGitHubName(gitHubName);

        if (krbName != null) {
            subscription.setRegisteredUser(new RegisteredUser());
            subscription.getRegisteredUser().setKrbName(krbName);
            subscription.setActiveKerberosAccount(hasKrbAccount);
        }

        return subscription;
    }
}
