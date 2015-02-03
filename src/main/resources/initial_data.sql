insert into application_parameters (param_name, param_value) values ('github.token', '148f1d234b8323edc72daac9a355df0cf59e8bea');
insert into application_parameters (param_name, param_value) values ('ldap.url', 'ldap://ldap.nrt.redhat.com');

insert into github_orgs (name) values ('thofmantestorg');

insert into github_teams (org_id, name, github_id) values (sq_github_orgs.currval, 'PushTeam', 1223195);
insert into github_teams (org_id, name, github_id) values (sq_github_orgs.currval, 'Owners', 1223193);
