package org.jboss.set.mjolnir.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.set.mjolnir.shared.domain.Report;
import org.jboss.set.mjolnir.shared.domain.ReportType;

import java.util.Map;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public interface ReportingServiceAsync {

    void generateReport(ReportType reportType, AsyncCallback<Report> callback);

    void getReportNames(AsyncCallback<Map<ReportType, String>> callback);

    void performReportAction(ReportType reportType, String uuid, String actionName, AsyncCallback<Void> callback);

}
