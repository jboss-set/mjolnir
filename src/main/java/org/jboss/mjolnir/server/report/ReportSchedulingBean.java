package org.jboss.mjolnir.server.report;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * Schedules report generating.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
@Singleton
@Startup
public class ReportSchedulingBean {

    @EJB
    private UnknownMembersReportBean unknownMembersReportBean;

    @Schedule(dayOfWeek = "Mon", hour = "0", minute = "0", second = "0", persistent = false)
    public void sendUnknownMembersReport() {
        unknownMembersReportBean.emailReport();
    }

}
