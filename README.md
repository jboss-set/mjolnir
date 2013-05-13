Mjolnir
=======

Login tool for a system to integrate Kerberos logins to Github usernames while adding them to a team of a specific organization on Github.

Building
-----------

This project is designed to be built by Apache Maven. The GWT maven plugins have been integrated already. To run:

mvn clean package

You can directly deploy to a running JBoss AS 7 (or EAP 6) instance by running:

mvn jboss-as:deploy
