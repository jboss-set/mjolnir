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


import org.jboss.mjolnir.authentication.GithubOrganization;
import org.jboss.mjolnir.authentication.GithubTeam;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Class to parse the xml data file.
 *
 * @author: navssurtani
 * @since: 0.1
 */
public class GithubParser {

    /* Constant Strings */
    private static final String ORGANIZATIONS = "organizations";
    private static final String ORGANIZATION = "organization";
    private static final String TEAM = "team";
    private static final String NAME = "name";
    private static final String ID = "id";

    private static final GithubParser INSTANCE = new GithubParser();

    public static GithubParser getInstance() {
        return INSTANCE;
    }

    private GithubParser() {
        // Singleton.
    }

    public Set<GithubOrganization> parse(String xmlFile) {
        Set<GithubOrganization> orgs = null;
        try {
            GithubOrganization org = null;
            File localFile = getAndCheckFile(xmlFile);
            InputStream in = new FileInputStream(localFile);

            // Create the XML Input Factory.
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            // Create the event reader.
            XMLEventReader reader = inputFactory.createXMLEventReader(in);

            // Now we do stuff.
            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                String data = null;

                if (checkEventType(event, ORGANIZATIONS)) {
                    orgs = new HashSet<GithubOrganization>();
                }

                if (checkEventType(event, ORGANIZATION)) {
                    data = reader.nextEvent().asCharacters().getData();
                    org = new GithubOrganization(data);
                }

                if (checkEventType(event, TEAM)) {
                    // We have found a team. Now we will extract the team name.
                    String teamName = null;
                    int teamId;
                    while (reader.hasNext()) {
                        event = reader.nextTag();
                        if (checkEventType(event, NAME)) {
                            teamName = reader.nextEvent().asCharacters().getData();
                        } else {
                            if (checkEventType(event, ID)) {
                                teamId = Integer.parseInt(reader.nextEvent().asCharacters().getData());
                                org.addTeam(new GithubTeam(teamName, teamId));
                                orgs.add(org);
                                // We now have all the information for this team - so we will break from this while block.
                                break;
                            }
                        }
                    }
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return orgs;
    }

    private boolean checkEventType(XMLEvent event, String constant) {
        if (event.isStartElement()) {
            return event.asStartElement().getName().getLocalPart().equals(constant);
        } else {
            return false;
        }
    }

    private File getAndCheckFile(String xmlFile) {
        URL url= GithubParser.class.getResource(xmlFile);
        String path = url.toExternalForm();
        path = path.substring(path.indexOf(":") + 1);
        return new File(path);
    }
}
