create table user_accounts (
    id                    uuid         primary key,
    email                 varchar(254) not null,
    username              varchar(30)  not null,
    display_name          varchar(80)  not null,
    password_hash         varchar(100) not null,
    status                varchar(30)  not null,
    email_verified_at     timestamptz,
    terms_version         varchar(20)  not null,
    terms_accepted_at     timestamptz  not null,
    failed_login_attempts integer      not null default 0,
    locked_until          timestamptz,
    created_at            timestamptz  not null,
    updated_at            timestamptz  not null,
    version               bigint       not null default 0,
    constraint uk_user_accounts_email    unique (email),
    constraint uk_user_accounts_username unique (username),
    constraint ck_user_accounts_status   check (status in ('PENDING_VERIFICATION', 'ACTIVE', 'SUSPENDED', 'DEACTIVATED'))
);

-- Rôles globaux uniquement. Les rôles contextuels (responsable d'une communauté,
-- d'un projet, administrateur d'une organisation) vivront dans leurs modules.
create table user_roles (
    user_id uuid        not null references user_accounts (id) on delete cascade,
    role    varchar(30) not null,
    primary key (user_id, role),
    constraint ck_user_roles_role check (role in ('MEMBER', 'MODERATOR', 'ADMIN'))
);

-- Une ligne par appareil connecté. Le refresh token n'est stocké que haché (SHA-256).
create table user_sessions (
    id                  uuid        primary key,
    user_id             uuid        not null references user_accounts (id) on delete cascade,
    refresh_token_hash  varchar(64) not null,
    previous_token_hash varchar(64),
    created_at          timestamptz not null,
    last_used_at        timestamptz not null,
    expires_at          timestamptz not null,
    revoked_at          timestamptz,
    user_agent          varchar(255),
    ip_address          varchar(45),
    constraint uk_user_sessions_refresh unique (refresh_token_hash)
);

create index idx_user_sessions_user     on user_sessions (user_id);
create index idx_user_sessions_previous on user_sessions (previous_token_hash);

-- Jetons à usage unique : vérification d'e-mail, réinitialisation du mot de passe.
create table one_time_tokens (
    id         uuid        primary key,
    user_id    uuid        not null references user_accounts (id) on delete cascade,
    purpose    varchar(30) not null,
    token_hash varchar(64) not null,
    created_at timestamptz not null,
    expires_at timestamptz not null,
    used_at    timestamptz,
    constraint uk_one_time_tokens_hash unique (token_hash),
    constraint ck_one_time_tokens_purpose check (purpose in ('EMAIL_VERIFICATION', 'PASSWORD_RESET'))
);

create index idx_one_time_tokens_user on one_time_tokens (user_id, purpose);
