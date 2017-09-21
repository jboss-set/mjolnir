package org.jboss.set.mjolnir.server.service;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.XsrfProtectedServiceServlet;
import org.jboss.set.mjolnir.shared.domain.KerberosUser;
import org.jboss.set.mjolnir.client.exception.ApplicationException;
import org.jboss.set.mjolnir.server.AuthenticationFilter;

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

    /**
     * {@inheritDoc}
     *
     * Overridden to ensure user authorization.
     */
    @Override
    public String processCall(RPCRequest rpcRequest) throws SerializationException {
        if (!performAuthorization()) {
            return RPC.encodeResponseForFailedRequest(rpcRequest, new ApplicationException("Not authorized."));
        }

        return super.processCall(rpcRequest);
    }

    /**
     * This method is to be overridden by extending implementations to ensure user authorization.
     *
     * @return is current user authorized to process call?
     */
    protected abstract boolean performAuthorization();
}
