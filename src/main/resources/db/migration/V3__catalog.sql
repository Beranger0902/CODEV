-- Référentiel partagé : domaines d'activité et technologies (catalogue §2, §4, §6, §8).
-- Listes curées, réutilisées pour les profils, questions, projets et filtres de recherche.

create table domains (
    id            uuid        primary key,
    slug          varchar(50) not null,
    name          varchar(80) not null,
    description   varchar(300),
    display_order integer     not null default 0,
    active        boolean     not null default true,
    created_at    timestamptz not null,
    updated_at    timestamptz not null,
    constraint uk_domains_slug unique (slug)
);

create table technologies (
    id         uuid        primary key,
    slug       varchar(60) not null,
    name       varchar(80) not null,
    kind       varchar(20) not null,
    active     boolean     not null default true,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uk_technologies_slug unique (slug),
    constraint ck_technologies_kind check (kind in
        ('LANGUAGE', 'FRAMEWORK', 'LIBRARY', 'TOOL', 'PLATFORM', 'DATABASE', 'OTHER'))
);

create index idx_technologies_kind on technologies (kind) where active;

-- Données de base. gen_random_uuid() et now() fournissent des valeurs initiales ;
-- l'administration pourra ensuite ajouter, renommer ou désactiver des entrées.
insert into domains (id, slug, name, description, display_order, active, created_at, updated_at) values
    (gen_random_uuid(), 'web',           'Développement web',       'Applications et sites web, frontend et backend', 10, true, now(), now()),
    (gen_random_uuid(), 'mobile',        'Développement mobile',    'Applications Android, iOS et multiplateformes',  20, true, now(), now()),
    (gen_random_uuid(), 'ai',            'Intelligence artificielle','Apprentissage automatique, données et modèles', 30, true, now(), now()),
    (gen_random_uuid(), 'data',          'Données',                 'Ingénierie des données, analytique, bases de données', 40, true, now(), now()),
    (gen_random_uuid(), 'cybersecurity', 'Cybersécurité',           'Sécurité offensive, défensive et conformité',    50, true, now(), now()),
    (gen_random_uuid(), 'cloud',         'Cloud',                   'Infrastructure et services cloud',               60, true, now(), now()),
    (gen_random_uuid(), 'devops',        'DevOps',                  'Intégration, déploiement et exploitation continue', 70, true, now(), now()),
    (gen_random_uuid(), 'systems',       'Systèmes et réseaux',     'Systèmes d''exploitation, réseaux, bas niveau',  80, true, now(), now()),
    (gen_random_uuid(), 'embedded',      'Systèmes embarqués',      'IoT, microcontrôleurs, temps réel',              90, true, now(), now()),
    (gen_random_uuid(), 'design',        'Design et UX',            'Design d''interface, expérience utilisateur',   100, true, now(), now()),
    (gen_random_uuid(), 'game',          'Jeux vidéo',              'Conception et développement de jeux',           110, true, now(), now()),
    (gen_random_uuid(), 'blockchain',    'Blockchain',              'Registres distribués et contrats intelligents', 120, true, now(), now());

insert into technologies (id, slug, name, kind, active, created_at, updated_at) values
    (gen_random_uuid(), 'java',        'Java',        'LANGUAGE',  true, now(), now()),
    (gen_random_uuid(), 'kotlin',      'Kotlin',      'LANGUAGE',  true, now(), now()),
    (gen_random_uuid(), 'python',      'Python',      'LANGUAGE',  true, now(), now()),
    (gen_random_uuid(), 'javascript',  'JavaScript',  'LANGUAGE',  true, now(), now()),
    (gen_random_uuid(), 'typescript',  'TypeScript',  'LANGUAGE',  true, now(), now()),
    (gen_random_uuid(), 'php',         'PHP',         'LANGUAGE',  true, now(), now()),
    (gen_random_uuid(), 'csharp',      'C#',          'LANGUAGE',  true, now(), now()),
    (gen_random_uuid(), 'cpp',         'C++',         'LANGUAGE',  true, now(), now()),
    (gen_random_uuid(), 'c',           'C',           'LANGUAGE',  true, now(), now()),
    (gen_random_uuid(), 'go',          'Go',          'LANGUAGE',  true, now(), now()),
    (gen_random_uuid(), 'rust',        'Rust',        'LANGUAGE',  true, now(), now()),
    (gen_random_uuid(), 'dart',        'Dart',        'LANGUAGE',  true, now(), now()),
    (gen_random_uuid(), 'sql',         'SQL',         'LANGUAGE',  true, now(), now()),
    (gen_random_uuid(), 'spring-boot', 'Spring Boot', 'FRAMEWORK', true, now(), now()),
    (gen_random_uuid(), 'angular',     'Angular',     'FRAMEWORK', true, now(), now()),
    (gen_random_uuid(), 'react',       'React',       'LIBRARY',   true, now(), now()),
    (gen_random_uuid(), 'vuejs',       'Vue.js',      'FRAMEWORK', true, now(), now()),
    (gen_random_uuid(), 'nodejs',      'Node.js',     'PLATFORM',  true, now(), now()),
    (gen_random_uuid(), 'django',      'Django',      'FRAMEWORK', true, now(), now()),
    (gen_random_uuid(), 'laravel',     'Laravel',     'FRAMEWORK', true, now(), now()),
    (gen_random_uuid(), 'flutter',     'Flutter',     'FRAMEWORK', true, now(), now()),
    (gen_random_uuid(), 'react-native','React Native','FRAMEWORK', true, now(), now()),
    (gen_random_uuid(), 'tensorflow',  'TensorFlow',  'LIBRARY',   true, now(), now()),
    (gen_random_uuid(), 'pytorch',     'PyTorch',     'LIBRARY',   true, now(), now()),
    (gen_random_uuid(), 'postgresql',  'PostgreSQL',  'DATABASE',  true, now(), now()),
    (gen_random_uuid(), 'mysql',       'MySQL',       'DATABASE',  true, now(), now()),
    (gen_random_uuid(), 'mongodb',     'MongoDB',     'DATABASE',  true, now(), now()),
    (gen_random_uuid(), 'redis',       'Redis',       'DATABASE',  true, now(), now()),
    (gen_random_uuid(), 'docker',      'Docker',      'TOOL',      true, now(), now()),
    (gen_random_uuid(), 'kubernetes',  'Kubernetes',  'TOOL',      true, now(), now()),
    (gen_random_uuid(), 'git',         'Git',         'TOOL',      true, now(), now()),
    (gen_random_uuid(), 'linux',       'Linux',       'PLATFORM',  true, now(), now());
