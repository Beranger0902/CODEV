-- Profil développeur et identité professionnelle (catalogue §2).
-- user_id référence un compte (module identity) mais SANS contrainte FK : les modules
-- restent indépendants côté schéma (règle Spring Modulith). L'intégrité est assurée par le code.

create table profiles (
    id                        uuid         primary key,
    user_id                   uuid         not null,
    headline                  varchar(120),
    bio                       varchar(2000),
    country_code              varchar(2),
    city                      varchar(80),
    timezone                  varchar(40),
    experience_level          varchar(20),
    availability              varchar(20)  not null default 'NOT_SPECIFIED',
    open_to_mentoring         boolean      not null default false,
    learning_goals            varchar(1000),
    collaboration_preferences varchar(1000),
    visibility                varchar(20)  not null default 'PUBLIC',
    location_visible          boolean      not null default true,
    created_at                timestamptz  not null,
    updated_at                timestamptz  not null,
    version                   bigint       not null default 0,
    constraint uk_profiles_user unique (user_id),
    constraint ck_profiles_experience check (experience_level is null or experience_level in
        ('STUDENT', 'JUNIOR', 'MID', 'SENIOR', 'LEAD')),
    constraint ck_profiles_availability check (availability in
        ('NOT_SPECIFIED', 'NOT_AVAILABLE', 'OPEN_TO_COLLABORATE', 'LOOKING_FOR_WORK', 'FREELANCE')),
    constraint ck_profiles_visibility check (visibility in ('PUBLIC', 'MEMBERS_ONLY', 'PRIVATE'))
);

-- Compétences déclarées : technologie du référentiel + niveau auto-évalué.
create table profile_skills (
    profile_id    uuid        not null references profiles (id) on delete cascade,
    technology_id uuid        not null,
    level         varchar(20) not null,
    primary key (profile_id, technology_id),
    constraint ck_profile_skills_level check (level in ('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT'))
);

-- Domaines d'activité du profil (référentiel catalog).
create table profile_domains (
    profile_id uuid not null references profiles (id) on delete cascade,
    domain_id  uuid not null,
    primary key (profile_id, domain_id)
);

-- Liens externes : GitHub, LinkedIn, portfolio, site…
create table profile_links (
    id            uuid         primary key,
    profile_id    uuid         not null references profiles (id) on delete cascade,
    type          varchar(20)  not null,
    url           varchar(300) not null,
    display_order integer      not null default 0,
    constraint ck_profile_links_type check (type in
        ('GITHUB', 'LINKEDIN', 'PORTFOLIO', 'WEBSITE', 'TWITTER', 'OTHER'))
);

create index idx_profile_links_profile on profile_links (profile_id);
