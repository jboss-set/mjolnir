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

When using `dev` profile, created WAR won't require HTTPS, enabling GWT codeserver to be used.

Running
-------

Application requires a datasource available under JNDI name "java:jboss/datasources/<appName>/MjolnirDS", where <appName> is name under which the application is deployed.

Database must be initialized with tables defined in src/main/resources/create_tables.sql and contain data from src/main/resources/initial_data.sql.

Also you need to insert a valid GitHub token into application_parameters table: https://help.github.com/articles/creating-an-access-token-for-command-line-use/

Creating a new organization tutorial: https://help.github.com/articles/creating-a-new-organization-from-scratch/

After successful deployment, the application can be used by browsing (by default) to:

```
http://localhost:8080/<appName>
```
