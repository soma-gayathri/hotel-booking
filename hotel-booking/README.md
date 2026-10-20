# Hotel Booking System (Spring Boot + PostgreSQL)

## Setup
1. Install Java 17+, Maven, PostgreSQL.
2. Create the database:  `createdb hotel_db`
3. Create tables:        `psql -d hotel_db -f db/schema.sql`
4. Edit `src/main/resources/application.properties` (DB username/password).
5. Run:                  `mvn spring-boot:run`   (server starts on http://localhost:8080)

## API
| Method | URL | Purpose |
|--------|-----|---------|
| POST | /api/auth/register | Create account |
| POST | /api/auth/login | Log in |
| GET  | /api/hotels?city=Tirupati | List/search hotels |
| POST | /api/hotels | Add hotel (admin) |
| POST | /api/rooms | Add room (admin) |
| GET  | /api/rooms/available?city=&checkIn=&checkOut=&guests= | Search available rooms |
| POST | /api/bookings | Book a room |
| GET  | /api/bookings/user/{userId} | My bookings |
| PUT  | /api/bookings/{id}/cancel | Cancel booking |

## Try it
```
curl -X POST localhost:8080/api/auth/register -H "Content-Type: application/json" \
  -d '{"fullName":"Asha","email":"asha@example.com","password":"secret123"}'

curl "localhost:8080/api/rooms/available?city=Tirupati&checkIn=2026-11-01&checkOut=2026-11-05&guests=2"

curl -X POST localhost:8080/api/bookings -H "Content-Type: application/json" \
  -d '{"userId":1,"roomId":1,"checkIn":"2026-11-01","checkOut":"2026-11-05","guests":2}'
```
Note: db/schema.sql inserts a sample user (id 1) with a fake password hash, so use the
register endpoint to create a real user for login. The sample booking query inside
schema.sql is also executed when you run the file; delete those lines if you want a clean start.
