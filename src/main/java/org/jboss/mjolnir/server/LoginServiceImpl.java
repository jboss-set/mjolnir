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
import org.jboss.mjolnir.authentication.GithubTeam;
import org.jboss.mjolnir.authentication.KerberosUser;
import org.jboss.mjolnir.authentication.LoginFailedException;
import org.jboss.mjolnir.client.LoginService;
import org.mindrot.jbcrypt.BCrypt;

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
import java.util.Set;

/**
 * @author: navssurtani
 * @since: 0.1
 */

public class LoginServiceImpl extends XsrfProtectedServiceServlet implements LoginService {

    private static final String XML_DATA = "/github-team-data.xml";

    private Cache<String, KerberosUser> cache;
    private Set<GithubOrganization> orgs;

    public LoginServiceImpl() {
        String cacheStoreLocation = null;
        try {
            cacheStoreLocation = getCacheStoreLocation();
        } catch (NamingException e) {
            e.printStackTrace();
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

        // Now do the parsing work.
        parse();
    }

    @Override
    public KerberosUser login(String krb5Name, String githubName, String password) throws LoginFailedException {
        log("login called with username " + krb5Name + " and github username " + githubName);
        try {
            validateCredentials(krb5Name, password);
        } catch (LoginException e) {
            log("LoginException caught from JaaS");
            throw new LoginFailedException("Error with login credentials.");
        } catch (URISyntaxException e) {
            log("URISyntaxException caught. Big problem here.");
            throw new LoginFailedException();
        }
        KerberosUser toReturn = cache.get(krb5Name);
        if (toReturn.getGithubName().equals(githubName)) {
            log("User " + krb5Name + " exists in cache.");
            return toReturn;
        }

        if (registerToGitHub(githubName)) {
            toReturn = register(krb5Name, githubName);
            log("KerberosUser has been verified with github and has been registered in the cache");
        } else {
            throw new LoginFailedException("Failed to register with GitHub. Please contact jboss-set@redhat.com");
        }

        log("Returning");
        return toReturn;
    }

    @Override
    public KerberosUser loginFromSession() {
        KerberosUser kerberosUser = null;
        HttpServletRequest request = this.getThreadLocalRequest();
        HttpSession session = request.getSession();
        Object userObject = session.getAttribute("kerberosUser");
        if (userObject != null && userObject instanceof KerberosUser) {
            kerberosUser = (KerberosUser) userObject;
        }
        return kerberosUser;
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

    private KerberosUser register(String krb5Name, String githubName) {
        log("Registering user of " + githubName + " to cache.");
        KerberosUser kerberosUser = new KerberosUser();
        kerberosUser.setName(krb5Name);
        kerberosUser.setGithubName(githubName);
        cache.put(krb5Name, kerberosUser);
        storeInSession(kerberosUser);
        return kerberosUser;
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

    private void storeInSession(KerberosUser kerberosUser) {
        HttpServletRequest httpServletRequest = this.getThreadLocalRequest();
        HttpSession session = httpServletRequest.getSession();
        session.setAttribute("kerberosUser", kerberosUser);
    }

    private void parse() {
        GithubParser parser = GithubParser.getInstance();
        orgs = parser.parse(XML_DATA);
    }

    private boolean registerToGitHub(String githubName) {
        // For now here we just want to deal with jboss-eap

        for (GithubOrganization org : orgs) {
            //TODO: Clean this up so we know which organization to look for in a cleaner manner.
            if (org.getName().equals("jbossas")) {
                log("The organization " + org.getName() + " has been found");
                GitHubClient client = new GitHubClient();
                client.setOAuth2Token(org.getToken());
                log("Setting OAuthToken as " + org.getToken());
                TeamService teamService = new TeamService(client);
                int teamId = 0;
                // We want to add to EAP View only
                for (GithubTeam t : org.getTeams()) {
                    if (t.getName().equals("EAP View")) teamId = t.getId();
                }

                try {
                    teamService.addMember(teamId, githubName);
                    log("Member of " + githubName + " successfully added to team " + teamId);
                    return true;
                } catch (IOException e) {
                    log("Unable to register " + githubName + " to team # " + teamId);
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public String getCacheStoreLocation() throws NamingException {
        // Get the environment naming context
        Context ctx = (Context) new InitialContext().lookup("java:comp/env");
        return (String) ctx.lookup("INFINISPAN_STORE");
    }
}
