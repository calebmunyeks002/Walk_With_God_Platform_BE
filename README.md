# WalkWithGod — Spring Boot API

Spring Boot 4.1.1 / Java 21 backend for the WalkWithGod Christian community platform.

## Local development

1. Start PostgreSQL with `docker compose up postgres -d`.
2. Set environment variables as needed (see `.env.example`).
3. Run `mvn spring-boot:run` from the backend folder.
4. Open Swagger at `http://localhost:8080/swagger-ui/index.html`.

The seeded development accounts are:

- `admin@walkwithgod.local` / `ChangeMe123!`
- `mentor@walkwithgod.local` / `ChangeMe123!`

Change these credentials immediately outside local development.

## Core API domains

- `/api/auth` — registration, login and current user
- `/api/users` — profile
- `/api/posts` — community posts and reactions
- `/api/mentors` — verified mentor discovery
- `/api/mentor-requests` — member-to-mentor requests
- `/api/mentor-applications` — mentor onboarding applications
- `/api/admin/mentor-applications` — verification workflow
- `/api/conversations` — inbox and messages
- `/api/bible` — licensed Bible provider proxy
- `/api/devotions` — devotion feed
- `/api/trivia/questions` — Bible trivia
- `/ws` — STOMP/WebSocket endpoint for real-time messaging extensions

## Mentor accreditation workflow

A normal member submits qualifications, Christian leadership/church affiliation, experience and evidence through the mentor application endpoint. Administrators review the application and, only after approval, the account receives the MENTOR role and a verified mentor profile.

The system should not represent platform verification as governmental or denominational accreditation. The final qualification policy, document requirements, reviewer roles, safeguarding rules and appeal process should be defined by the organization operating WalkWithGod.

## Bible licensing

Do not bundle copyrighted Bible translations into the source repository unless your organization has the required rights. The API is structured to proxy a licensed provider such as API.Bible. Configure `BIBLE_API_KEY` and `BIBLE_ID` in the deployment environment. API.Bible provides a large catalog of Bible translations through an API and works with rights holders.

## 100k+ user production architecture

Run multiple stateless API instances behind a load balancer; keep JWT verification local; use PostgreSQL with connection-pool limits, indexes and read replicas; use Redis for hot feeds, sessions/rate-limit counters and fan-out; use object storage + CDN for avatars/images; use a managed message broker for WebSocket events; process email/push notifications asynchronously; use an observability stack for logs, metrics and traces; use database migrations and automated backups; enforce pagination/cursor pagination on feeds; apply abuse controls, content moderation, reporting, blocking, audit logs and data retention policies before public launch.
