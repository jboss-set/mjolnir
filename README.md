Mjolnir
=======

This tool allows users to self-subscribe to selected GitHub organizations according to their needs. It also contains
an administration interface allowing browsing, searching and managing GitHub subscriptions and can generate report
on subscribed users. 

CI Job: https://travis-ci.org/jboss-set/mjolnir

Building
-----------

This project is designed to be built by Apache Maven (developed on 3.0.4). You can deploy to an already running Wildfly 8, JBoss AS7 or JBoss EAP 6 server by the following:

```
mvn clean jboss-as:deploy
```

When using `dev` profile, created WAR won't require HTTPS, enabling GWT codeserver to be used. It also limits number
of GWT permutation that are compiled to only Firefox and Google Chrome versions.

Running
-------

Application requires a datasource available under JNDI name "java:jboss/datasources/&lt;appName&gt;/MjolnirDS", where &lt;appName&gt; is name under which the application is deployed.

Database must be initialized with tables defined in src/main/resources/create_tables.sql and contain data from src/main/resources/initial_data.sql.

Also you need to insert a valid GitHub token into application_parameters table: https://help.github.com/articles/creating-an-access-token-for-command-line-use/

Creating a new organization tutorial: https://help.github.com/articles/creating-a-new-organization-from-scratch/

After successful deployment, the application can be used by browsing (by default) to:

```
http://localhost:8080/<appName>
```
