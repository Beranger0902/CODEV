-- Journal des opérations sensibles (catalogue §1, complément §25).
-- Ne jamais y écrire de mot de passe, jeton ou donnée privée inutile.
create table audit_log (
    id          uuid         primary key,
    occurred_at timestamptz  not null,
    actor_id    uuid,
    action      varchar(60)  not null,
    target_type varchar(40),
    target_id   uuid,
    ip_address  varchar(45),
    details     varchar(500)
);

create index idx_audit_log_actor  on audit_log (actor_id, occurred_at desc);
create index idx_audit_log_target on audit_log (target_type, target_id);
