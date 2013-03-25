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

package org.jboss.mjolnir;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.GitHubResponse;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Main class to be used for this project. In time, we might not need a Main class.
 *
 * @author: navssurtani
 * @since: 0.1
 */
public class Main {

    private static final String BASE_URL = "https://api.github.com";
    private static final String PROPS_FILE = "users.properties";

    /* The actual repository base can be appended after this bit */
    private static String repository = "repos";
    private static String project = null;

    public static void main(String[] args) {
        Properties properties = new Properties();
        try {
            properties.load(streamFromFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
        GitHubClient client = new GitHubClient();
        client.setCredentials(properties.getProperty("NAME"), properties.getProperty("PASSWORD"));
        System.out.println("Name and password are: " + properties.getProperty("NAME") + " " + properties.getProperty("PASSWORD"));

        GitHubRequest request = new GitHubRequest();
        request.setUri(buildURI(properties));

        GitHubResponse response = null;
        try {
            response = client.get(request);
            Object body = response.getBody();
            System.out.println("Response body is looks like: " + body.toString());

        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new RuntimeException("There has been an error in executing the request to Github. " +
                    "Your URL might be wrong. " + request.getUri());
        }
    }

    private static String buildURI(Properties properties) {
        repository = repository + "/" + properties.getProperty("REPOSITORY_BASE");
        project = properties.getProperty("PROJECT");
        return BASE_URL + "/" + repository + "/" + project;
    }

    private static InputStream streamFromFile() {
        String path = null;
        try {

            path = Main.class.getResource(PROPS_FILE).toString();
            path = path.substring(path.indexOf(":") + 1);
            return new FileInputStream(path);

        } catch (Exception e) {
            if (path.equals(null)) {
                throw new NullPointerException();
            } else {
                e.printStackTrace();
            }
        }
        return null;
    }

}
