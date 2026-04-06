# Library Microservices (Java + Spring Boot + MySQL + JWT + Eureka Service Discovery + Optional Kafka)

This repo contains a small library system split into microservices with **Netflix Eureka** service discovery:

- `eureka-server` (Service Registry & Discovery) on `:8761`
- `auth-service` (JWT issuer) on `:8090`
- `api-gateway` (JWT validation + role-based access + routes via Eureka) on `:8080`
- `user-service` on `:8081` (uses MySQL + JDBC)
- `book-service` on `:8082` (uses MySQL + JDBC)
- `borrow-service` on `:8083` (uses MySQL + JDBC + scheduler + optional Kafka)
- `fine-service` on `:8084` (uses MySQL + JDBC + Kafka consumer, optional)
- `notification-service` on `:8085` (Kafka consumer, sends SMTP email)

Kafka is **optional**. If you keep `app.kafka.enabled=false` (default), you can still test the REST flow (borrow/issue/return + fines endpoints) but the event-driven updates won’t run:
- user `current_count` updates (book issued/returned listeners)
- book copy status updates (issue/return listeners)
- overdue fine accrual (fine-service) (and Kafka-driven email notifications)

Even when Kafka is OFF, `notification-service` can still send the **daily overdue reminder email** via Spring Scheduler (2:00 AM) by calling `borrow-service` and using SMTP.

When Kafka is enabled, fine accrual and Kafka-driven cross-service updates can also run.

---

## Prerequisites

1. Java 17+
2. Maven
3. MySQL running on `localhost:3306`
4. (Optional) Kafka running on `localhost:9092` (or set `KAFKA_BOOTSTRAP_SERVERS`)
5. `curl` for testing

---

## Start Here: what to do first

1. Prepare MySQL + seed/demo data
2. Build all services (including eureka-server)
3. **Start `eureka-server` FIRST** (on port 8761)
4. Start `api-gateway` + `auth-service` (to get a JWT)
5. Start the business services (`user`, `book`, `borrow`, `fine`, `notification`)
6. **Verify services registered with Eureka** (see section 4)
7. Test the REST flow through the gateway
8. Only then enable Kafka + SMTP for the full event-driven behavior

---

## 1) Create/prepare MySQL databases

### Option A (recommended): run init script

Run once as MySQL root:

```bash
mysql -u root -p < /absolute/path/to/library2/scripts/init-mysql.sql
```

This creates databases:

- `library_users`
- `library_books`
- `library_borrows`
- `library_fines`
- `library_notify` (reserved)

and a MySQL user `library` with password `library` (used by all services by default).

### Option B: if your `users` table already exists without `password_hash`

Run:

```sql
-- run on library_users
ALTER TABLE users ADD COLUMN password_hash VARCHAR(255) NULL AFTER is_blocked;
```

The repo also has `scripts/alter-users-add-password.sql`.

---

## 2) JWT setup (shared secret)

Both:

- `auth-service` (`auth-service/src/main/resources/application.yml`)
- `api-gateway` (`api-gateway/src/main/resources/application.yml`)

must use the **same** `app.jwt.secret-base64`.

Defaults are already set in both files. For a quick local test, you can keep defaults.

If you change the secret, set `JWT_SECRET_BASE64` in your environment for both services.

---

## 3) Seed users for login

`user-service` seeds demo users via `data.sql` on startup:

- `librarian@library.local` / `password` (role: `LIBRARIAN`)
- `patron@library.local` / `password` (role: `PATRON`)

`auth-service` reads `users.password_hash` and `users.role`.

---

## 4) Build

From repo root:

```bash
# Build Eureka Server first
mvn -q -DskipTests -f eureka-server/pom.xml compile

# Build all microservices
mvn -q -DskipTests -f auth-service/pom.xml compile
mvn -q -DskipTests -f api-gateway/pom.xml compile
mvn -q -DskipTests -f user-service/pom.xml compile
mvn -q -DskipTests -f book-service/pom.xml compile
mvn -q -DskipTests -f borrow-service/pom.xml compile
mvn -q -DskipTests -f fine-service/pom.xml compile
mvn -q -DskipTests -f notification-service/pom.xml compile
```

If `mvn package` fails with a "repackage rename" error, delete stale `target/` folders and retry:

```bash
rm -rf */target
mvn -q -DskipTests -f eureka-server/pom.xml package
```

---

## 5) Run services (REST-only mode: Kafka OFF)

Keep `app.kafka.enabled: false` in every service (it’s the default).

Start in any order, but typically:

1. `api-gateway`
2. `auth-service`
3. `user-service`
4. `book-service`
5. `borrow-service`
6. `fine-service`
7. `notification-service`

Example run commands (terminal per service):

```bash
mvn -q -f api-gateway/pom.xml spring-boot:run
```

Repeat with:

- `auth-service/pom.xml`
- `user-service/pom.xml`
- `book-service/pom.xml`
- `borrow-service/pom.xml`
- `fine-service/pom.xml`
- `notification-service/pom.xml`

---

## 6) Test authentication + RBAC (Gateway)

### Login (public)

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"librarian@library.local","password":"password"}'
```

This returns `accessToken`.

Store it in a variable:

```bash
export TOKEN="PASTE_ACCESS_TOKEN_HERE"
```

### Create a user (LIBRARIAN only)

```bash
curl -s -X POST http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"email":"newpatron@library.local","role":"PATRON","maxLimit":3,"password":"password"}'
```

If you use a `PATRON` token here, gateway should reject (RBAC).

---

## 7) Test REST borrow flow (Kafka OFF)

Even without Kafka, you can test the REST endpoints directly:

1. Create a book (LIBRARIAN)
2. Add a copy (LIBRARIAN)
3. Create a borrow request (PATRON or LIBRARIAN)
4. Issue (LIBRARIAN)
5. Return (authenticated)
6. View fines / pay fine (fine endpoints exist)

### 7.1 Create book

```bash
curl -s -X POST http://localhost:8080/api/v1/books \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"isbn":"9781234567890","title":"DDD in Practice","author":"Jane Doe"}'
```

### 7.2 Add copy

Use the returned `book.id` from the previous response.

```bash
curl -s -X POST http://localhost:8080/api/v1/books/1/copies \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"barcode":"BC-0001"}'
```

### 7.3 Login as patron

```bash
export PATRON_TOKEN="$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"patron@library.local","password":"password"}' \
  | python3 -c \"import sys,json; print(json.load(sys.stdin)['accessToken'])\")"
```

### 7.4 Create borrow request

Use the patron `userId` and copy `barcode`.

Example:

```bash
curl -s -X POST http://localhost:8080/api/v1/borrows \
  -H "Authorization: Bearer $PATRON_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"userId":2,"barcode":"BC-0001","loanDays":14}'
```

### 7.5 Issue borrow

```bash
curl -s -X POST http://localhost:8080/api/v1/borrows/1/issue \
  -H "Authorization: Bearer $TOKEN"
```

### 7.6 Return borrow

```bash
curl -s -X POST http://localhost:8080/api/v1/borrows/1/return \
  -H "Authorization: Bearer $PATRON_TOKEN"
```

Note: with Kafka OFF, `user-service.current_count` and `book-service.book_copies.status` are not updated via events.

---

## 8) Kafka end-to-end test (event-driven)

To test event-driven behavior (your designed flow), you must enable Kafka consumers/producers.

### 8.1 Enable Kafka in configs

Set `app.kafka.enabled: true` in these services:

- `borrow-service/src/main/resources/application.yml` (producer)
- `user-service/src/main/resources/application.yml` (listeners)
- `book-service/src/main/resources/application.yml` (listeners)
- `fine-service/src/main/resources/application.yml` (overdue consumer)
- `notification-service/src/main/resources/application.yml` (overdue consumer)

Also ensure `KAFKA_BOOTSTRAP_SERVERS` points to your Kafka brokers.

### 8.1b Configure SMTP (so notification-service can send real mail)

`notification-service` sends email using Spring Mail. Set these env vars (or edit `notification-service/src/main/resources/application.yml`):

- `MAIL_HOST` (e.g. `smtp.gmail.com`)
- `MAIL_PORT` (e.g. `587`)
- `MAIL_USERNAME` (SMTP username/email)
- `MAIL_PASSWORD` (SMTP/app password)
- `MAIL_FROM` (sender address)

---

### 8.2 Create Kafka topics

Expected topic names from YAML:

- `library.book.issued`
- `library.book.returned`
- `library.overdue`

Example (run using your Kafka install scripts):

```bash
kafka-topics.sh --bootstrap-server localhost:9092 --create --topic library.book.issued --partitions 1 --replication-factor 1
kafka-topics.sh --bootstrap-server localhost:9092 --create --topic library.book.returned --partitions 1 --replication-factor 1
kafka-topics.sh --bootstrap-server localhost:9092 --create --topic library.overdue --partitions 1 --replication-factor 1
```

If topics already exist, Kafka will fail; in that case just skip creation.

---

### 8.3 What to verify in Kafka mode

Run the same borrow flow as in section 7, but now verify side effects:

1. When you call `POST /api/v1/borrows/{id}/issue` (LIBRARIAN):
   - `borrow-service` publishes `BOOK_ISSUED` to `library.book.issued`
   - `book-service` listener sets copy status `ISSUED`
   - `user-service` listener increments `users.current_count`

2. When you call `POST /api/v1/borrows/{id}/return`:
   - `borrow-service` publishes `BOOK_RETURNED` to `library.book.returned`
   - `book-service` listener sets copy status `AVAILABLE`
   - `user-service` listener decrements `users.current_count`

3. Overdue fines + email:
   - `borrow-service` scheduler (`app.borrow.overdue-cron`) queries overdue loans
   - it publishes `OVERDUE_EVENT` to `library.overdue`
   - `fine-service` inserts a daily fine row idempotently (unique `(lending_id, accrual_date)`)
   - `notification-service` consumes the event and sends the email via SMTP

---

## 9) Overdue testing (practical tips)

The scheduler runs daily based on:

- `borrow-service/src/main/resources/application.yml`: `app.borrow.overdue-cron` (default: `0 0 1 * * *` at 1:00 AM UTC)

For faster testing:

1. Temporarily set the cron to every minute (in `borrow-service/application.yml`)
2. Or update the DB to make an issued borrow overdue:
   - set `borrow_records.status='ALLOCATED'`
   - set `borrow_records.due_date` to a past timestamp

Then wait for the scheduler to publish `OVERDUE_EVENT`.

You can also quickly list overdue rows using:

- `GET /api/v1/borrows/overdue` on the gateway

---

## 10) Known limitations / things to watch

1. Kafka delivery is at-least-once; if you enable Kafka in a real environment, you should add idempotency for `user-service` borrow count updates too.
2. `notification-service` needs SMTP credentials (MAIL_HOST/MAIL_USERNAME/MAIL_PASSWORD/etc.) to send real emails.
3. `mvn package` can fail due to `spring-boot-maven-plugin:repackage` rename issues in some environments; `mvn compile` usually works and is enough for running via `spring-boot:run`.

---

## Quick endpoint reference (all via `api-gateway`)

- `POST /api/v1/auth/login`
- `GET  /api/v1/users/{id}`
- `POST /api/v1/users` (LIBRARIAN)
- `POST /api/v1/books` (LIBRARIAN)
- `POST /api/v1/books/{bookId}/copies` (LIBRARIAN)
- `POST /api/v1/borrows` (PATRON or LIBRARIAN)
- `POST /api/v1/borrows/{id}/issue` (LIBRARIAN)
- `POST /api/v1/borrows/{id}/return` (authenticated)
- `GET  /api/v1/borrows/overdue` (LIBRARIAN)
- `GET  /api/v1/users/{userId}/fines`
- `POST /api/v1/fines/{id}/pay`

