package org.jboss.set.mjolnir.server;

import org.jboss.set.mjolnir.shared.domain.RegisteredUser;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AdminOnlyFilter extends AuthenticationFilter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        super.doFilter(servletRequest, servletResponse, filterChain);
        RegisteredUser authenticatedUser = getAuthenticatedUser((HttpServletRequest) servletRequest);
        if (authenticatedUser != null && authenticatedUser.isAdmin()) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            ((HttpServletResponse) servletResponse).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
