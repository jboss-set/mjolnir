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


package org.jboss.mjolnir.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.server.rpc.XsrfProtect;
import org.jboss.mjolnir.authentication.GithubOrganization;
import org.jboss.mjolnir.authentication.KerberosUser;
import org.jboss.mjolnir.authentication.LoginFailedException;
import org.jboss.mjolnir.authentication.TokenServiceUtil;

import java.util.Set;

/**
 * @author: navssurtani
 * @since: 0.1
 */

@RemoteServiceRelativePath("LoginService")
@XsrfProtect
public interface LoginService extends RemoteService {

    boolean login(String krb5Name, String password) throws LoginFailedException;
    boolean isRegistered(String krb5Name);
    KerberosUser getKerberosUser(String krb5Name);
    KerberosUser registerKerberosUser(String krb5Name, String githubName) throws RuntimeException;
    void logout();
    void setSession();
    void subscribe(String orgName, int teamId, String githubName);
    void unsubscribe(String orgName, int teamId, String githubName);
    Set<GithubOrganization> getAvailableOrganizations();

    public static class Util {
        private static LoginServiceAsync instance;

        public static LoginServiceAsync getInstance() {
            if (instance == null) {
                instance = (LoginServiceAsync) GWT.create(LoginService.class);
                TokenServiceUtil.add( (HasRpcToken) instance);
            }
            return instance;
        }
    }
}
