package org.jboss.mjolnir.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.mjolnir.client.domain.SubscriptionSummary;

import java.util.List;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public interface AdministrationServiceAsync {

    void getSubscriptionsSummary(AsyncCallback<List<SubscriptionSummary>> async);

}
