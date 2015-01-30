package org.jboss.mjolnir.server.service;

import com.google.gwt.user.server.rpc.XsrfProtectedServiceServlet;
import org.jboss.mjolnir.authentication.KerberosUser;
import org.jboss.mjolnir.server.AuthenticationFilter;

import javax.servlet.http.HttpSession;

/**
 * Base class for service servlets.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public abstract class AbstractServiceServlet extends XsrfProtectedServiceServlet {

    protected HttpSession getSession() {
        return getThreadLocalRequest().getSession(true);
    }

    protected KerberosUser getAuthenticatedUser() {
        final Object user = getSession().getAttribute(AuthenticationFilter.AUTHENTICATED_USER_SESSION_KEY);
        if (user instanceof KerberosUser) {
            return (KerberosUser) user;
        }
        return null;
    }

    protected void setAuthenticatedUser(KerberosUser user) {
        getSession().setAttribute(AuthenticationFilter.AUTHENTICATED_USER_SESSION_KEY, user);
    }

}
