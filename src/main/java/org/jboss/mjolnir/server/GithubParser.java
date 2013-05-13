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


import org.jboss.logging.Logger;
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
    private static final String TOKEN = "token";
    private static final String TEAM = "team";
    private static final String NAME = "name";
    private static final String ID = "id";

    private static final Logger log = Logger.getLogger(GithubParser.class);

    private static final GithubParser INSTANCE = new GithubParser();

    public static GithubParser getInstance() {
        log.info("getInstance() called on GithubParser");
        return INSTANCE;
    }

    private GithubParser() {
        log.info("Parser instantiated.");
        // Singleton.
    }

    public Set<GithubOrganization> parse(String xmlFile) {
        log.info("parse() called within GithubParser.");
        Set<GithubOrganization> orgs = null;
        try {
            GithubOrganization org = null;
            InputStream in = getAndCheckFile(xmlFile);;

            // Create the XML Input Factory.
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            // Create the event reader.
            XMLEventReader reader = inputFactory.createXMLEventReader(in);

            // Now we do stuff.
            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                String data = null;

                if (checkEventType(event, ORGANIZATIONS)) {
                    log.debug("Creating Set for GithubOrganizations.");
                    orgs = new HashSet<GithubOrganization>();
                }

                if (checkEventType(event, ORGANIZATION)) {
                    data = reader.nextEvent().asCharacters().getData();
                    log.debug("Found a new GithubOrganization, creating it.");
                    org = new GithubOrganization(data);
                }

                // The OAuth token part.
                if (checkEventType(event, TOKEN)) {
                    data = reader.nextEvent().asCharacters().getData();
                    org.setToken(data);
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
                                log.info("Team of " + teamName + " and " + teamId + " added to " + org.getName());
                                // We now have all the information for this team - so we will break from this while block.
                                break;
                            }
                        }
                    }
                }
            }

        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return orgs;
    }

    private boolean checkEventType(XMLEvent event, String constant) {
        log.trace("Checking event type inside the Github Parser.");
        if (event.isStartElement()) {
            return event.asStartElement().getName().getLocalPart().equals(constant);
        } else {
            return false;
        }
    }

    private InputStream getAndCheckFile(String xmlFile) {
        InputStream stream = GithubParser.class.getResourceAsStream(xmlFile);
        if (stream != null) {
            if (log.isTraceEnabled()) log.trace("XML file of name " + xmlFile + " found. All good to return.");
        }
        else throw new NullPointerException("Null InputStream from file " + xmlFile);
        return stream;
    }
}
