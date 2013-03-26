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

package org.jboss.mjolnir.authentication;


import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.jboss.mjolnir.util.PropertiesProcessor;

import javax.swing.JPasswordField;
import java.io.IOException;
import java.util.List;

/**
 * Class/bean that will hold login information when someone wants to access github.
 * <p/>
 * For now we will just use the Github authentication and integrate kerberos with time.
 *
 * @author: navssurtani
 * @since: 0.1
 */

public class UserLogin {

    private String user;

    // Keep this data as character arrays.
    private JPasswordField password;
    private JPasswordField authToken;

    private GitHubClient client;

    public UserLogin(String user, String password) {
        if (user.equals(null) || password.equals(null)) throw new NullPointerException("Null parameters");
        this.user = user;
        this.password = new JPasswordField(password);
        client = new GitHubClient();
        client.setCredentials(user, password.toString());
    }

    public UserLogin(String authToken) {
        if (authToken.equals(null)) throw new NullPointerException("Null parameters");
        this.authToken = new JPasswordField(authToken);

        client = new GitHubClient();
        client.setOAuth2Token(authToken.toString());
    }

    public List<PullRequest> getPullRequests() throws IOException {
        PullRequestService prs = new PullRequestService(client);
        IRepositoryIdProvider repoProvider = RepositoryId.create(PropertiesProcessor.getRepositoryBase()
                , PropertiesProcessor.getProjectName());
        return prs.getPullRequests(repoProvider, "open");
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password.toString();
    }

    public String getAuthToken() {
        return authToken.toString();
    }
}
