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


package org.jboss.set.mjolnir.client.service;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.server.rpc.XsrfProtect;
import org.jboss.set.mjolnir.shared.domain.GithubOrganization;
import org.jboss.set.mjolnir.shared.domain.KerberosUser;
import org.jboss.set.mjolnir.client.XsrfUtil;
import org.jboss.set.mjolnir.shared.domain.EntityUpdateResult;
import org.jboss.set.mjolnir.client.exception.ApplicationException;

import java.util.Set;

/**
 * Service allowing user to modify his subscriptions.
 *
 * @author navssurtani
 * @author Tomas Hofman (thofman@redhat.com)
 */

@RemoteServiceRelativePath("auth/GitHubService")
@XsrfProtect
public interface GitHubService extends RemoteService {

    EntityUpdateResult<KerberosUser> modifyGitHubName(String newGithubName) throws ApplicationException;
    String subscribe(int teamId) throws ApplicationException;
    void unsubscribe(int teamId) throws ApplicationException;
    Set<GithubOrganization> getAvailableOrganizations() throws ApplicationException;
    Set<GithubOrganization> getSubscriptions() throws ApplicationException;

    class Util {
        private static GitHubServiceAsync instance;

        public static GitHubServiceAsync getInstance() {
            if (instance == null) {
                instance = GWT.create(GitHubService.class);
            }
            XsrfUtil.putToken((HasRpcToken) instance);
            return instance;
        }
    }
}
