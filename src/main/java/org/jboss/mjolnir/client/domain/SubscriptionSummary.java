package org.jboss.mjolnir.client.domain;

import org.jboss.mjolnir.authentication.GithubOrganization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class SubscriptionSummary implements Serializable {

    private GithubOrganization organization;
    private List<Subscription> subscriptions;

    public GithubOrganization getOrganization() {
        return organization;
    }

    public void setOrganization(GithubOrganization organization) {
        this.organization = organization;
    }

    public List<Subscription> getSubscriptions() {
        if (subscriptions == null) {
            subscriptions = new ArrayList<Subscription>();
        }
        return subscriptions;
    }

}
