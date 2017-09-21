package org.jboss.set.mjolnir.server;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Servlet filter checking that user is signed in.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class AuthenticationFilter implements Filter {

    public static final String AUTHENTICATED_USER_SESSION_KEY = "MJOLNIR_USER";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;
        final HttpSession session = request.getSession();
        if (session != null) {
            if (session.getAttribute(AUTHENTICATED_USER_SESSION_KEY) != null) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
        }
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Override
    public void destroy() {
    }
}
