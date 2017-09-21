package org.jboss.set.mjolnir.client.application.admin.registeredUsers;

import java.util.List;

import com.gwtplatform.mvp.client.UiHandlers;
import org.jboss.set.mjolnir.shared.domain.Subscription;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public interface RegisteredUsersHandlers extends UiHandlers {
    void whitelist(List<Subscription> subscriptions, boolean whitelist);
    void delete(List<Subscription> items);
    void edit(Subscription item);
    void register(Subscription item);
}
