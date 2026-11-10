-- Projets collaboratifs : fiches, rôles recherchés, candidatures et membres (catalogue §7, complément §24).
-- Références inter-modules (owner_id, user_id, technology_id, domain_id) sans FK : modules indépendants.

create table projects (
    id                uuid         primary key,
    owner_id          uuid         not null,
    name              varchar(120) not null,
    problem           varchar(500),
    description       varchar(5000),
    objectives        varchar(2000),
    features          varchar(2000),
    status            varchar(20)  not null default 'DRAFT',
    visibility        varchar(20)  not null default 'PUBLIC',
    team_size_limit   integer,
    repo_url          varchar(300),
    demo_url          varchar(300),
    applications_open boolean      not null default true,
    created_at        timestamptz  not null,
    updated_at        timestamptz  not null,
    version           bigint       not null default 0,
    constraint ck_projects_status check (status in
        ('DRAFT', 'RECRUITING', 'IN_PROGRESS', 'COMPLETED', 'ARCHIVED')),
    constraint ck_projects_visibility check (visibility in ('PUBLIC', 'MEMBERS_ONLY', 'PRIVATE')),
    constraint ck_projects_team_size check (team_size_limit is null or team_size_limit > 0)
);

create index idx_projects_status on projects (status) where status <> 'ARCHIVED';

create table project_domains (
    project_id uuid not null references projects (id) on delete cascade,
    domain_id  uuid not null,
    primary key (project_id, domain_id)
);

create table project_technologies (
    project_id    uuid not null references projects (id) on delete cascade,
    technology_id uuid not null,
    primary key (project_id, technology_id)
);

-- Rôles recherchés par le projet, avec compétences requises.
create table project_roles (
    id            uuid        primary key,
    project_id    uuid        not null references projects (id) on delete cascade,
    title         varchar(80) not null,
    description   varchar(1000),
    headcount     integer     not null default 1,
    display_order integer     not null default 0,
    constraint ck_project_roles_headcount check (headcount > 0)
);

create index idx_project_roles_project on project_roles (project_id);

create table project_role_skills (
    role_id       uuid not null references project_roles (id) on delete cascade,
    technology_id uuid not null,
    primary key (role_id, technology_id)
);

-- Membres de l'équipe. Le créateur est OWNER. L'accès à l'espace privé suit une acceptation.
create table project_members (
    id         uuid        primary key,
    project_id uuid        not null references projects (id) on delete cascade,
    user_id    uuid        not null,
    role       varchar(20) not null,
    joined_at  timestamptz not null,
    constraint uk_project_members unique (project_id, user_id),
    constraint ck_project_members_role check (role in ('OWNER', 'MAINTAINER', 'CONTRIBUTOR'))
);

create index idx_project_members_user on project_members (user_id);

-- Candidatures. Une candidature ne donne pas accès : seule l'acceptation crée le membre.
create table project_applications (
    id          uuid         primary key,
    project_id  uuid         not null references projects (id) on delete cascade,
    applicant_id uuid        not null,
    role_id     uuid,
    message     varchar(1000),
    status      varchar(20)  not null default 'PENDING',
    created_at  timestamptz  not null,
    decided_by  uuid,
    decided_at  timestamptz,
    constraint ck_project_applications_status check (status in
        ('PENDING', 'ACCEPTED', 'REJECTED', 'WITHDRAWN'))
);

create index idx_project_applications_project on project_applications (project_id);
create index idx_project_applications_applicant on project_applications (applicant_id);

-- Une seule candidature en attente par (projet, candidat).
create unique index uk_project_applications_pending
    on project_applications (project_id, applicant_id) where status = 'PENDING';
