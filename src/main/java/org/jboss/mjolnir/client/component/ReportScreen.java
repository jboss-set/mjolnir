package org.jboss.mjolnir.client.component;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class ReportScreen extends Composite {

    private ReportingServiceAsync reportingService = ReportingService.Util.getInstance();

    public ReportScreen(final ReportType reportType, String reportName) {
        final HTMLPanel panel = new HTMLPanel("");
        initWidget(panel);

        panel.add(new HTMLPanel("h2", reportName));

        final HTMLPanel resultPanel = new HTMLPanel("");

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
                                resultPanel.clear();
                                final HTMLPanel prePanel = new HTMLPanel("pre", result.getContent());
                                prePanel.getElement().getStyle().setProperty("white-space", "pre-wrap");
                                resultPanel.add(prePanel);
                            }
                        });
                    }
                });
            }
        }));

        panel.add(resultPanel);
    }
}
