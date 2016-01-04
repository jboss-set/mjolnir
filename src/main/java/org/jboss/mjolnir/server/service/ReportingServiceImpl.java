package org.jboss.mjolnir.server.service;

import org.jboss.mjolnir.shared.domain.Report;
import org.jboss.mjolnir.shared.domain.ReportType;
import org.jboss.mjolnir.client.exception.ApplicationException;
import org.jboss.mjolnir.client.exception.ReportDataNotAvailableException;
import org.jboss.mjolnir.client.service.ReportingService;
import org.jboss.mjolnir.server.report.AbstractReportBean;
import org.jboss.mjolnir.server.report.UnknownMembersReportBean;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class ReportingServiceImpl extends AbstractAdminRestrictedService implements ReportingService {

    private static final String REPORT_DATA_KEY = "MJOLNIR_REPORT_DATA";

    @EJB
    private UnknownMembersReportBean unknownMembersReportBean;

    private Map<ReportType, AbstractReportBean<?>> reportBeanMap = new HashMap<ReportType, AbstractReportBean<?>>();

    @Override
    public void init() throws ServletException {
        super.init();
        reportBeanMap.put(ReportType.UNKNOWN_MEMBERS, unknownMembersReportBean);
    }

    @Override
    public Report generateReport(ReportType reportType) {
        final AbstractReportBean<?> reportBean = getReportBean(reportType);
        final Report<?> report = reportBean.generateReport();
        storeReportData(reportType, report);
        return report;
    }

    @Override
    public Map<ReportType, String> getReportNames() {
        final Map<ReportType, String> reportNames = new HashMap<ReportType, String>();
        for (Map.Entry<ReportType, AbstractReportBean<?>> entry: reportBeanMap.entrySet()) {
            reportNames.put(entry.getKey(), entry.getValue().getReportName());
        }
        return reportNames;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void performReportAction(ReportType reportType, String uuid, String actionName) throws ReportDataNotAvailableException {
        final Object reportData = getReportData(reportType, uuid);
        if (reportData == null) {
            throw new ReportDataNotAvailableException("Report data are not available for report " + uuid + " of type " + reportType.name());
        }
        final AbstractReportBean reportBean = getReportBean(reportType);
        reportBean.performReportAction(actionName, reportData);
    }

    private AbstractReportBean getReportBean(ReportType reportType) {
        if (reportBeanMap.containsKey(reportType)) {
            return reportBeanMap.get(reportType);
        }
        throw new ApplicationException("Unknown report: " + reportType);
    }


    /**
     * Stores result of last report of given type into session.
     *
     * @param reportType report type
     * @param report generated report
     */
    @SuppressWarnings("unchecked")
    private void storeReportData(ReportType reportType, Report<?> report) {
        Map<ReportType, Map<String, Object>> reportTypeMap =
                (Map<ReportType, Map<String, Object>>) getSession().getAttribute(REPORT_DATA_KEY);
        if (reportTypeMap == null) {
            reportTypeMap = new HashMap<ReportType, Map<String, Object>>();
            getSession().setAttribute(REPORT_DATA_KEY, reportTypeMap);
        }

        Map<String, Object> reportDataMap = reportTypeMap.get(reportType);
        if (reportDataMap == null) {
            reportDataMap = new HashMap<String, Object>();
            reportTypeMap.put(reportType, reportDataMap);
        }

        reportDataMap.clear();
        reportDataMap.put(report.getUuid(), report.getData());
    }

    /**
     * Retrieves data of the last report of given type, if they are still available.
     *
     * @param reportType report type
     * @param uuid report uuid
     * @return report data or null if not available
     */
    @SuppressWarnings("unchecked")
    private Object getReportData(ReportType reportType, String uuid) {
        Map<ReportType, Map<String, Object>> reportTypeMap =
                (Map<ReportType, Map<String, Object>>) getSession().getAttribute(REPORT_DATA_KEY);
        if (reportTypeMap == null) {
            return null;
        }

        Map<String, Object> reportDataMap = reportTypeMap.get(reportType);
        if (reportDataMap == null) {
            return null;
        }

        return reportDataMap.get(uuid);
    }

}
