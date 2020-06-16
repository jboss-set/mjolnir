package org.jboss.set.mjolnir.server.report;

import javax.ejb.EJB;
import javax.ejb.Singleton;

/**
 * Schedules report generating.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
@Singleton
//@Startup
public class ReportSchedulingBean {

    @EJB
    private UnknownMembersReportBean unknownMembersReportBean;

    // Report generation disabled
//    @Schedule(dayOfWeek = "Mon", hour = "0", minute = "0", second = "0", persistent = false)
    public void sendUnknownMembersReport() {
        unknownMembersReportBean.emailReport();
    }

}
