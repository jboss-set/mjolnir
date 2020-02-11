# GitHub Teams Subscriber

This tool allows users to self-subscribe to selected GitHub organizations according to their needs. It also contains
an administration interface allowing browsing, searching and managing GitHub subscriptions and can generate report
on subscribed users.

The application requires:
* access to a database (for storing configuration and user states),
* access to an LDAP server,
* a SAML server for authentication.

CI Job: https://travis-ci.org/jboss-set/mjolnir

## Building

Requires Apache Maven 3.0 or higher.

You should first create `keycloak-saml.xml` file in `saml-config/` directory before building (it can be exported from 
your Keycloak server), or alternatively use the existing sample keycloak-saml file by using 
`-Dsaml.file=keycloak-saml-example.xml` switch:

```
mvn clean package -Dsaml.file=keycloak-saml-example.xml
```

You can then deploy to an already running Wildfly 8, JBoss AS7 or JBoss EAP 6 server by the following:

```
mvn wildfly:deploy
```

or just copy the war file to a deployment folder of the application server.

## Running

### Prepare Keycloak server

Download and run Keycloak server. Sample users and a client should be created.

### Prepare Database

Application requires a datasource available under JNDI name "java:jboss/datasources/&lt;appName&gt;/MjolnirDS", where &lt;appName&gt; is name under which the application is deployed.

Use this example to create the datasource (change the connection URL and JNDI name if needed):

```
data-source add --name=MjolnirDS --jndi-name=java:jboss/datasources/mjolnir/MjolnirDS --driver-name=h2 --connection-url=jdbc:h2:tcp://localhost/~/Projects/mjolnir/db/dbdata --user-name=sa
```

Database must be initialized with tables defined in src/main/resources/create_tables.sql and contain data from src/main/resources/initial_data.sql.

Also you need to insert a valid GitHub token into application_parameters table: https://help.github.com/articles/creating-an-access-token-for-command-line-use/

Creating a new organization tutorial: https://help.github.com/articles/creating-a-new-organization-from-scratch/

After successful deployment, the application can be used by browsing (by default) to:

```
http://localhost:8080/<archiveName>
```

Development / Debugging
-----------

1. Connect to VPN to get access to KRB server.
2. Start H2 database and insert configuration data (see initial_data.sql in resources).
   ```
   $ ./start_db.sh
   ```
3. Start Wildfly and deploy the war.
4. Run GWT Dev Mode:
   ```
   $ mvn gwt:run-codeserver
   ```
5. In Google Chrome, navigate to http://localhost:8080/mjolnir/ and activate GWT Dev Mode.