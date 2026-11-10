-- Tâches, jalons et dépendances (catalogue §7, partie 2).
-- Espace de travail privé de l'équipe : l'accès est réservé aux membres du projet (contrôlé par le code).

create table milestones (
    id            uuid         primary key,
    project_id    uuid         not null references projects (id) on delete cascade,
    title         varchar(120) not null,
    description   varchar(2000),
    due_date      date,
    status        varchar(20)  not null default 'OPEN',
    display_order integer      not null default 0,
    created_at    timestamptz  not null,
    updated_at    timestamptz  not null,
    constraint ck_milestones_status check (status in ('OPEN', 'COMPLETED'))
);

create index idx_milestones_project on milestones (project_id);

create table tasks (
    id          uuid         primary key,
    project_id  uuid         not null references projects (id) on delete cascade,
    milestone_id uuid        references milestones (id) on delete set null,
    title       varchar(200) not null,
    description varchar(5000),
    status      varchar(20)  not null default 'TODO',
    priority    varchar(10)  not null default 'MEDIUM',
    assignee_id uuid,
    due_date    date,
    board_order integer      not null default 0,
    created_by  uuid         not null,
    created_at  timestamptz  not null,
    updated_at  timestamptz  not null,
    version     bigint       not null default 0,
    constraint ck_tasks_status check (status in ('TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE')),
    constraint ck_tasks_priority check (priority in ('LOW', 'MEDIUM', 'HIGH', 'URGENT'))
);

create index idx_tasks_project_status on tasks (project_id, status);
create index idx_tasks_milestone on tasks (milestone_id);
create index idx_tasks_assignee on tasks (assignee_id);

-- Dépendances entre tâches : une tâche est bloquée par celles dont elle dépend.
create table task_dependencies (
    task_id            uuid not null references tasks (id) on delete cascade,
    depends_on_task_id uuid not null references tasks (id) on delete cascade,
    primary key (task_id, depends_on_task_id),
    constraint ck_task_dependencies_self check (task_id <> depends_on_task_id)
);
