package org.jboss.mjolnir.client.service;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.server.rpc.XsrfProtect;
import org.jboss.mjolnir.client.domain.SubscriptionSummary;

import java.util.List;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
@RemoteServiceRelativePath("AdministrationService")
@XsrfProtect
public interface AdministrationService extends RemoteService {

    List<SubscriptionSummary> getSubscriptionsSummary();

    public static class Util {
        private static AdministrationServiceAsync instance;

        public static AdministrationServiceAsync getInstance() {
            if (instance == null) {
                instance = GWT.create(AdministrationService.class);
            }
            return instance;
        }
    }
}
