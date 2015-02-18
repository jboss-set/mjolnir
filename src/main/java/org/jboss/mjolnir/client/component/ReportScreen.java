package org.jboss.mjolnir.client.component;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import org.jboss.mjolnir.client.ExceptionHandler;
import org.jboss.mjolnir.client.XsrfUtil;
import org.jboss.mjolnir.client.domain.Report;
import org.jboss.mjolnir.client.domain.ReportType;
import org.jboss.mjolnir.client.service.ReportingService;
import org.jboss.mjolnir.client.service.ReportingServiceAsync;

/**
 * Screen displaying report results.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class ReportScreen extends Composite {

    private ReportingServiceAsync reportingService = ReportingService.Util.getInstance();
    private HTMLPanel resultPanel;

    public ReportScreen(final ReportType reportType, String reportName) {
        final HTMLPanel panel = new HTMLPanel("");
        initWidget(panel);

        panel.add(new HTMLPanel("h2", reportName));

        resultPanel = new HTMLPanel("");

        panel.add(new Button("Generate Report", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                resultPanel.clear();
                resultPanel.add(new LoadingPanel());
                XsrfUtil.obtainToken(new XsrfUtil.Callback() {
                    @Override
                    public void onSuccess(XsrfToken token) {
                        ((HasRpcToken) reportingService).setRpcToken(token);
                        reportingService.generateReport(reportType, new AsyncCallback<Report>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                ExceptionHandler.handle(caught);
                            }

                            @Override
                            public void onSuccess(Report result) {
                                printReport(result, reportType);
                            }
                        });
                    }
                });
            }
        }));

        panel.add(resultPanel);
    }

    private void printReport(Report<?> report, ReportType reportType) {
        resultPanel.clear();
        final HTMLPanel prePanel = new HTMLPanel("pre", report.getContent());
        prePanel.getElement().getStyle().setProperty("white-space", "pre-wrap");
        resultPanel.add(prePanel);

        if (report.getActions() != null && report.getActions().size() > 0) {
            resultPanel.add(new HTMLPanel("h3", "Report Actions"));
            for (String actionName: report.getActions()) {
                final HTMLPanel actionButtonPara = new HTMLPanel("p", "");
                final Button actionButton = new Button(actionName);
                actionButton.addClickHandler(new RunActionClickHandler(report, reportType, actionName));
                actionButtonPara.add(actionButton);
                resultPanel.add(actionButtonPara);
            }
        }
    }

    /**
     * Handler that calls report action.
     */
    private class RunActionClickHandler implements ClickHandler {

        private Report<?> report;
        private ReportType reportType;
        private String actionName;

        private RunActionClickHandler(Report<?> report, ReportType reportType, String actionName) {
            this.report = report;
            this.reportType = reportType;
            this.actionName = actionName;
        }

        @Override
        public void onClick(ClickEvent event) {
            if (Window.confirm("This might have far reaching consequences. Proceed?")) {
                XsrfUtil.obtainToken(new XsrfUtil.Callback() {
                    @Override
                    public void onSuccess(XsrfToken token) {
                        ((HasRpcToken) reportingService).setRpcToken(token);
                        reportingService.performReportAction(reportType, report.getUuid(), actionName, new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                ExceptionHandler.handle("Action failed.", caught);
                            }

                            @Override
                            public void onSuccess(Void result) {
                                Window.alert("Action successful.");
                            }
                        });
                    }
                });
            }
        }
    }
}
