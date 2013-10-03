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

package org.jboss.mjolnir.server;

import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.server.rpc.XsrfProtectedServiceServlet;
import com.sun.security.auth.login.ConfigFile;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.TeamService;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.mjolnir.authentication.GithubOrganization;
import org.jboss.mjolnir.authentication.KerberosUser;
import org.jboss.mjolnir.authentication.LoginFailedException;
import org.jboss.mjolnir.client.LoginService;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author: navssurtani
 * @since: 0.1
 */

public class LoginServiceImpl extends XsrfProtectedServiceServlet implements LoginService {


    private Cache<String, KerberosUser> cache;
    private Map<String, GithubOrganization> orgs;

    public LoginServiceImpl() {
        String cacheStoreLocation;
        try {
            cacheStoreLocation = getCacheStoreLocation();
        } catch (NamingException e) {
            e.printStackTrace();
            throw new InstantiationError("Could not instantiate servlet due to error with cache store location.");
        }
        GlobalConfigurationBuilder global = new GlobalConfigurationBuilder();
        global.globalJmxStatistics()
                .allowDuplicateDomains(true).jmxDomain("org.jboss.mjolnir");
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.loaders().preload(true)
                .addFileCacheStore().location(cacheStoreLocation)
                .eviction().maxEntries(50);
        Configuration config = builder.build(true);
        EmbeddedCacheManager cacheManager = new DefaultCacheManager(config);
        cache = cacheManager.getCache();

        orgs = new HashMap<String, GithubOrganization>();
        // TODO: Should the Parser be returning a Map?
        try {
            for (GithubOrganization o : GithubParser.getOrganizations()) {
                orgs.put(o.getName(), o);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new InstantiationError("Could not instantiate servlet due to error with GithubParser.");
        }

    }

    @Override
    public boolean login(String krb5Name, String password) throws LoginFailedException {
        // This will always be the first method called by the user upon hitting the web-app.
        // We will return true if the kerberos password is correct. Regardless of whether or not their details
        // already exist in the cache.

        log("login() called on servlet with username " + krb5Name);
        try {
            validateCredentials(krb5Name, password);
        } catch (LoginException e) {
            log("LoginException caught from JaaS. Problem with login credentials.");
            log(e.getMessage());

            // The user-password combination is not correct. We should simply return false and allow the user to
            // re-enter their password.
            return false;

        } catch (URISyntaxException e) {
            // Here there is a problem, so the onFailure() part will be called on the client side
            log("URISyntaxException caught. Big problem here.");
            throw new LoginFailedException("There is a problem with the login on the server. Please contact " +
                    "jboss-set@redhat.com");
        }
        log("Login succeeded. Returning 'true'");
        return true;
    }

    @Override
    public boolean isRegistered(String krb5Name) {
        // As long as the cache contains the String, we return true.
        boolean toReturn = cache.containsKey(krb5Name);
        log("Value whether or not " + krb5Name + " is registered: " + toReturn);
        return cache.containsKey(krb5Name);
    }

    @Override
    public KerberosUser getKerberosUser(String krb5Name) {
        KerberosUser toReturn = cache.get(krb5Name);
        log("User to return:" + toReturn.toString());
        return toReturn;
    }

    @Override
    public KerberosUser registerKerberosUser(String krb5Name, String githubName) {
        log("Registering user of " + krb5Name + ", " + githubName + " to cache.");
        KerberosUser kerberosUser = new KerberosUser();
        kerberosUser.setName(krb5Name);
        kerberosUser.setGithubName(githubName);
        cache.put(krb5Name, kerberosUser);
        log("User to return back to client is: " + kerberosUser.toString());
        return kerberosUser;
    }

    @Override
    public KerberosUser modifyGithubName(String krb5Name, String newGithubName) {
        // First get the object out of the cache.
        KerberosUser ku = cache.get(krb5Name);
        log("Changing githubName for KerberosUser " + krb5Name + ". Old name is " + ku.getGithubName() + ". New name " +
                "is " + newGithubName);
        ku.setGithubName(newGithubName);
        // Now put it back into the cache.
        cache.put(krb5Name, ku);
        log("Successfully modified GithubName for KerberosUser " + krb5Name);
        return ku;
    }

    @Override
    public void logout() {
        HttpServletRequest request = this.getThreadLocalRequest();
        HttpSession session = request.getSession(true);
        session.removeAttribute("kerberosUser");
    }

    @Override
    public void setSession() {
        log("setSession() called.");
        String cookieValue = getThreadLocalRequest().getRequestedSessionId();
        Cookies.setCookie("JSESSIONID", cookieValue);
        log("Session ID cookie set as: " + cookieValue);
    }

    @Override
    public void subscribe(String orgName, int teamId, String githubName) {
        TeamService teamService = obtainTeamService(orgName);
        try {
            teamService.addMember(teamId, githubName);
            log("Successfully added " + githubName + " to team.");
        } catch (IOException e) {
            throw new RuntimeException("Unable to subscribe user " + githubName
                    + " to team #" + teamId + " of organization " + orgName, e);
        }
    }

    @Override
    public void unsubscribe(String orgName, int teamId, String githubName) {
        TeamService teamService = obtainTeamService(orgName);
        try {
            teamService.removeMember(teamId, githubName);
            log("Successfully removed " + githubName + " from team.");
        } catch (IOException e) {
            throw new RuntimeException("Unable to unsubscribe user " + githubName
                    + " to team #" + teamId + " of organization " + orgName, e);
        }
    }

    @Override
    public Set<GithubOrganization> getAvailableOrganizations() {
        log("Returning organizations. The collection has " + orgs.size() + " entries");
        return new HashSet<GithubOrganization>(orgs.values());
    }

    // Method that will only be called if someone tries to log into the application for the first time.
    private void validateCredentials(final String krb5Name, final String password)
            throws LoginException, URISyntaxException {
        log("Validating credentials.");
        final Subject subject = null;
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
        final LoginContext loginContext = new LoginContext("Kerberos", subject, callbackHandler, loginConfiguration);
        loginContext.login();
        log("Kerberos credentials ok for " + krb5Name);
    }

    public String getCacheStoreLocation() throws NamingException {
        // Get the environment naming context
        Context ctx = (Context) new InitialContext().lookup("java:comp/env");
        return (String) ctx.lookup("INFINISPAN_STORE");
    }
    private TeamService obtainTeamService(String orgName) {
        GithubOrganization organization = orgs.get(orgName);
        GitHubClient client = new GitHubClient();
        client.setOAuth2Token(organization.getToken());
        log("Returning TeamService object for organization " + orgName);
        return new TeamService(client);
    }
}
