create sequence sq_github_orgs;

create table github_orgs (
    id bigint default nextval('sq_github_orgs') primary key,
    name varchar(255) unique,
    subscriptions_enabled boolean default true;
);

create sequence sq_github_teams;

create table github_teams (
    id bigint default nextval('sq_github_teams') primary key,
    org_id bigint not null,
    name varchar(255),
    github_id bigint unique,
    selfservice boolean,
    constraint fk_github_teams_org_id foreign key (org_id) references github_orgs (id)
);

create sequence sq_users;

create table users (
    id bigint default nextval('sq_users') primary key,
    krb_name varchar(255) unique,
    employee_number int unique,
    github_name varchar(255) unique,
    github_id int unique,
    note varchar(255),
    admin boolean not null default false,
    whitelisted boolean not null default false,
    responsible_person varchar(255)
);

create table application_parameters (
    param_name varchar(255) primary key,
    param_value varchar(255)
);