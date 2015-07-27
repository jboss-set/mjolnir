insert into application_parameters (param_name, param_value) values ('github.token', '***');
insert into application_parameters (param_name, param_value) values ('ldap.url', 'ldap://ldap.nrt.redhat.com');
insert into application_parameters (param_name, param_value) values ('infinispan.path', '/home/jboss/infinispan.store');
insert into application_parameters (param_name, param_value) values ('application.reporting_email', 'jboss-set@redhat.com');
insert into application_parameters (param_name, param_value) values ('krb5.realm', 'REDHAT.COM');
insert into application_parameters (param_name, param_value) values ('krb5.kdc', 'kerberos01.core.prod.int.ams2.redhat.com');

insert into github_orgs (name) values ('some org');

insert into github_teams (org_id, name, github_id) values (sq_github_orgs.currval, 'some team', 123);
insert into github_teams (org_id, name, github_id) values (sq_github_orgs.currval, 'some other team', 1234);
