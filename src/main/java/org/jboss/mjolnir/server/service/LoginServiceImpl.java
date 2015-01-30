/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.mjolnir.server.service;

import com.sun.security.auth.login.ConfigFile;
import org.jboss.mjolnir.authentication.KerberosUser;
import org.jboss.mjolnir.authentication.LoginFailedException;
import org.jboss.mjolnir.client.service.LoginService;
import org.jboss.mjolnir.server.bean.UserRepository;

import javax.ejb.EJB;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

/**
 * Provides authentication methods.
 *
 * @author navssurtani
 * @author Tomas Hofman (thofman@redhat.com)
 */

public class LoginServiceImpl extends AbstractServiceServlet implements LoginService {

    @EJB
    private UserRepository userRepository;

    @Override
    public KerberosUser login(String krb5Name, String password) throws LoginFailedException {
        // This will always be the first method called by the user upon hitting the web-app.
        // We will return true if the kerberos password is correct. Regardless of whether or not their details
        // already exist in the cache.

        log("login() called on servlet with username " + krb5Name);
        final KerberosUser user;
        try {
            validateCredentials(krb5Name, password);
            user = userRepository.getOrCreateUser(krb5Name);
            setAuthenticatedUser(user);
        } catch (LoginException e) {
            log("LoginException caught from JaaS. Problem with login credentials.");
            log(e.getMessage());

            // The user-password combination is not correct. We should simply return false and allow the user to
            // re-enter their password.
            return null;
        } catch (URISyntaxException e) {
            // Here there is a problem, so the onFailure() part will be called on the client side
            log("URISyntaxException caught. Big problem here.");
            throw new LoginFailedException("There is a problem with the login on the server. Please contact " +
                    "jboss-set@redhat.com");
        } catch (SQLException e) {
            throw new LoginFailedException(e.getMessage());
        }
        log("Login succeeded. Returning 'true'");
        return user;
    }

    @Override
    public KerberosUser getLoggedUser() {
        return getAuthenticatedUser();
    }

    @Override
    public void logout() {
        setAuthenticatedUser(null);
    }

    // Method that will only be called if someone tries to log into the application for the first time.
    private void validateCredentials(final String krb5Name, final String password)
            throws LoginException, URISyntaxException {
        log("Validating credentials.");
        final CallbackHandler callbackHandler = new CallbackHandler() {
            @Override
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for (Callback cb : callbacks) {
                    if (cb instanceof NameCallback) {
                        ((NameCallback) cb).setName(krb5Name);
                    } else if (cb instanceof PasswordCallback) {
                        ((PasswordCallback) cb).setPassword(password.toCharArray());
                    } else {
                        throw new IllegalStateException("Unknown callback.");
                    }
                }
            }
        };
        final javax.security.auth.login.Configuration loginConfiguration = new ConfigFile(this.getClass()
                .getResource("/jaas.config").toURI());
        final LoginContext loginContext = new LoginContext("Kerberos", null, callbackHandler, loginConfiguration);
        loginContext.login();
        log("Kerberos credentials ok for " + krb5Name);
    }

}
