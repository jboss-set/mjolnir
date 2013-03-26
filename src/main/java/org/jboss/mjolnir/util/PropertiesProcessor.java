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

package org.jboss.mjolnir.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Class that will read the properties file which has the basic meta-data that we have at the moment.
 *
 * @author: navssurtani
 * @since: 0.1
 */

public class PropertiesProcessor {

    /** The properties object */
    private static Properties properties;

    public static void loadProperties(String propertiesFile) {
        properties = new Properties();
        try {
            properties.load(streamFromFile(propertiesFile));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static String getName() {
        return properties.getProperty("NAME");
    }

    public static String getPassword() {
        return properties.getProperty("PASSWORD");
    }

    public static String getToken() {
        return properties.getProperty("AUTH_TOKEN");
    }

    public static String getRepositoryBase() {
        return properties.getProperty("REPOSITORY_BASE");
    }

    public static String getProjectName() {
        return properties.getProperty("PROJECT");
    }

    // If we need to modify the token.
    public static void setToken(char[] token) {
        properties.setProperty("AUTH_TOKEN", String.copyValueOf(token));
    }

    public static boolean hasToken() {
        return (!getToken().equals("-1"));
    }

    private static InputStream streamFromFile(String propertiesFile) {
        String path = null;
        try {
            path = PropertiesProcessor.class.getResource(propertiesFile).toString();
            path = path.substring(path.indexOf(":") + 1);
            return new FileInputStream(path);
        } catch (FileNotFoundException e) {
            System.out.println("File cannot be found with path: " + path);
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }


}
