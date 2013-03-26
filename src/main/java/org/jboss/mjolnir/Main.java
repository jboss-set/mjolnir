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

import org.eclipse.egit.github.core.PullRequest;
import org.jboss.mjolnir.authentication.UserLogin;
import org.jboss.mjolnir.util.PropertiesProcessor;

import java.io.IOException;
import java.util.List;

/**
 * Main class to be used for this project. In time, we might not need a Main class.
 *
 * @author: navssurtani
 * @since: 0.1
 */
public class Main {

    private static final String PROPS_FILE_NAME = "/users.properties";

    public static void main(String[] args) {
        PropertiesProcessor.loadProperties(PROPS_FILE_NAME);
        UserLogin user = null;
        if (PropertiesProcessor.hasToken()) {
            System.out.println("Has token");
            user = new UserLogin(PropertiesProcessor.getToken());
        } else {
            user = new UserLogin(PropertiesProcessor.getName(), PropertiesProcessor.getPassword());
        }
        try {
            final List<PullRequest> pulls = user.getPullRequests();
            for (PullRequest pull : pulls) {
                System.out.println("Pull request: " + pull.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }


}
