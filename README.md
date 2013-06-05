Mjolnir
=======

This is a tool that is run as a web-app which will allow Github users to be authenticated to specific teams within organizations. The server that this application is running on requires Kerberos installed on it first. Kerberos should be configured in the file jaas.config.

Note that the organizations and teams which this application can handle are determined through XML. The xml file to configure is called github-team-data.xml.


Building
-----------

This project is designed to be built by Apache Maven (developed on 3.0.4). You can deploy to an already running Wildfly 8, JBoss AS7 or JBoss EAP 6 server by the following:

```
mvn clean jboss-as:deploy
```

Running
-------

After successful deployment, the application can be used by browsing (by default) to:

```
http://localhost:8080/mjolnir
```
