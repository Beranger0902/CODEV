# NovaCore — Backend

API REST de la plateforme NovaCore : Java 25, Spring Boot 4.1, PostgreSQL.
Architecture : **monolithe modulaire** vérifié par Spring Modulith (cf. complément technique §23).

## Prérequis

| Outil | Version | Remarque |
|---|---|---|
| JDK | 25 (LTS) | [Eclipse Temurin](https://adoptium.net/) ; définir `JAVA_HOME` |
| Maven | — | inutile : utiliser le wrapper `mvnw` / `mvnw.cmd` fourni |
| Docker Desktop | récent | recommandé : démarre PostgreSQL et Mailpit automatiquement |

## Démarrage rapide

### Option A — avec Docker (recommandé)

```bash
./mvnw spring-boot:run          # Windows : mvnw.cmd spring-boot:run
```

Spring Boot lance `compose.yaml` (PostgreSQL 17 + Mailpit), applique les migrations Flyway puis démarre l'API.

### Option B — PostgreSQL installé localement, sans Docker

```sql
CREATE USER novacore WITH PASSWORD 'novacore';
CREATE DATABASE novacore OWNER novacore;
```

```powershell
$env:DOCKER_COMPOSE_ENABLED="false"
.\mvnw.cmd spring-boot:run
```

Sans Mailpit, les e-mails ne partent pas (l'erreur est journalisée). Utilisez un SMTP de test
ou installez [Mailpit](https://mailpit.axllent.org/) en local (port 1025).

### Adresses utiles (profil dev)

- API : http://localhost:8080/api/v1
- Documentation Swagger : http://localhost:8080/swagger-ui.html
- E-mails capturés (Mailpit) : http://localhost:8025
- Santé : http://localhost:8080/actuator/health

## Structure

```
com.novacore
├── shared/          Socle ouvert : erreurs (ProblemDetail), sécurité JWT, audit, ClientInfo
├── identity/        Comptes, authentification, sessions, mots de passe
│   ├── api/           Contrôleurs + DTO (les entités ne sont jamais exposées)
│   ├── application/   Cas d'usage, règles, émission des jetons
│   └── domain/        Entités JPA + repositories
└── notification/    Envoi d'e-mails (API publique : MailService)
```

Règle Modulith : un module n'utilise que le **package racine** d'un autre module (son API publique).
`ModularityTests` échoue si cette règle est violée.

Les migrations sont dans `src/main/resources/db/migration` (`V<n>__description.sql`).
Hibernate est en `ddl-auto=validate` : **toute modification de schéma passe par une migration Flyway.**

## API d'identité

| Méthode | Route | Accès | Rôle |
|---|---|---|---|
| POST | `/api/v1/auth/register` | public | Inscription (202, e-mail de vérification) |
| POST | `/api/v1/auth/verify-email` | public | Confirmation de l'adresse |
| POST | `/api/v1/auth/verify-email/resend` | public | Renvoi du lien |
| POST | `/api/v1/auth/login` | public | Jeton d'accès (15 min) + refresh token (30 j) |
| POST | `/api/v1/auth/refresh` | public | Rotation des jetons |
| POST | `/api/v1/auth/logout` | public | Révoque la session de l'appareil |
| POST | `/api/v1/auth/password/forgot` | public | Lien de réinitialisation |
| POST | `/api/v1/auth/password/reset` | public | Nouveau mot de passe, révoque toutes les sessions |
| GET | `/api/v1/me` | connecté | Compte courant |
| PUT | `/api/v1/me/password` | connecté | Changement, révoque les autres sessions |
| GET | `/api/v1/me/sessions` | connecté | Appareils connectés |
| DELETE | `/api/v1/me/sessions/{id}` | connecté | Déconnecter un appareil |

Erreurs : format RFC 9457 avec un champ `code` stable (`INVALID_CREDENTIALS`, `EMAIL_NOT_VERIFIED`,
`VALIDATION_FAILED` + `errors[]`, etc.).

## Choix de sécurité (complément §25)

- Mots de passe hachés en BCrypt via `DelegatingPasswordEncoder` (migration vers Argon2 possible sans casse).
- Refresh tokens et jetons d'e-mail : 256 bits aléatoires, **stockés uniquement hachés** (SHA-256).
- Rotation du refresh token à chaque usage ; réutilisation d'un ancien jeton = session révoquée.
- Verrouillage temporaire après 5 échecs ; réponses identiques que l'e-mail existe ou non.
- Jetons à usage unique, invalidés quand un nouveau est émis ; délai anti-spam entre deux envois.
- E-mails envoyés seulement **après commit** de la transaction.
- Journal d'audit (`audit_log`) des opérations sensibles, sans secret.
- Secret JWT obligatoire en variable d'environnement (`NOVACORE_JWT_SECRET`, ≥ 32 octets) ; l'application refuse de démarrer sinon (hors profil dev).

**Limites connues / à faire :**
- Un jeton d'accès reste valide jusqu'à son expiration (15 min) après révocation de la session.
- Limitation de débit par IP (Redis + Bucket4j) à ajouter devant `/api/v1/auth/**`.
- MFA pour les comptes modérateur/administrateur.
- Refresh token en cookie `HttpOnly` pour le client web (aujourd'hui dans le corps JSON, adapté au mobile).

## Tests

```bash
./mvnw test
```

- Tests unitaires et `ModularityTests` : aucun prérequis.
- `AuthFlowIntegrationTests` (parcours complet sur PostgreSQL) : nécessite Docker, **ignorés automatiquement sinon**.
- Les diagrammes des modules sont générés dans `target/spring-modulith-docs`.

## Production

Profil `prod` (`SPRING_PROFILES_ACTIVE=prod`) : variables listées dans `.env.example`.
Swagger est désactivé, l'API attend un reverse proxy TLS de confiance (`forward-headers-strategy: native`).

## Prochains modules

`catalog` (technologies, domaines) → `profile` → `project` (candidatures, membres, tâches) → `qa` → `moderation`.

## Licence

Distribué sous licence **Apache 2.0** — voir [LICENSE](LICENSE) et [NOTICE](NOTICE).
