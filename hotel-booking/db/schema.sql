-- Hotel booking: 4 tables (PostgreSQL)
CREATE EXTENSION IF NOT EXISTS btree_gist;  -- needed for the double-booking constraint

CREATE TABLE users (
    user_id       SERIAL PRIMARY KEY,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    phone         VARCHAR(20),
    password_hash TEXT NOT NULL,
    role          VARCHAR(10) NOT NULL DEFAULT 'guest' CHECK (role IN ('guest','admin')),
    created_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE hotels (
    hotel_id    SERIAL PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    city        VARCHAR(100) NOT NULL,
    address     TEXT,
    description TEXT,
    star_rating SMALLINT CHECK (star_rating BETWEEN 1 AND 5)
);

CREATE TABLE rooms (
    room_id         SERIAL PRIMARY KEY,
    hotel_id        INT NOT NULL REFERENCES hotels(hotel_id) ON DELETE CASCADE,
    room_number     VARCHAR(10) NOT NULL,
    room_type       VARCHAR(50) NOT NULL,          -- e.g. Standard, Deluxe, Suite
    max_guests      SMALLINT NOT NULL DEFAULT 2,
    price_per_night NUMERIC(10,2) NOT NULL CHECK (price_per_night > 0),
    UNIQUE (hotel_id, room_number)
);

CREATE TABLE bookings (
    booking_id  SERIAL PRIMARY KEY,
    user_id     INT NOT NULL REFERENCES users(user_id),
    room_id     INT NOT NULL REFERENCES rooms(room_id),
    check_in    DATE NOT NULL,
    check_out   DATE NOT NULL,
    guests      SMALLINT NOT NULL DEFAULT 1,
    total_price NUMERIC(10,2) NOT NULL,
    status      VARCHAR(12) NOT NULL DEFAULT 'confirmed'
                CHECK (status IN ('confirmed','cancelled','completed')),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    CHECK (check_out > check_in),
    -- Database refuses overlapping bookings for the same room
    EXCLUDE USING gist (
        room_id WITH =,
        daterange(check_in, check_out) WITH &&
    ) WHERE (status <> 'cancelled')
);

CREATE INDEX idx_hotels_city    ON hotels(city);
CREATE INDEX idx_bookings_room  ON bookings(room_id, check_in, check_out);

-- ---------------------------------------------------------------
-- SAMPLE DATA
-- ---------------------------------------------------------------
INSERT INTO users (full_name, email, phone, password_hash) VALUES
  ('Ravi Kumar', 'ravi@example.com', '9876543210', 'hashed_password_here');

INSERT INTO hotels (name, city, address, star_rating) VALUES
  ('Hotel Sai Residency', 'Tirupati', 'Near Railway Station', 3),
  ('Grand Palace',        'Hyderabad', 'Banjara Hills', 5);

INSERT INTO rooms (hotel_id, room_number, room_type, max_guests, price_per_night) VALUES
  (1, '101', 'Standard', 2, 1500),
  (1, '102', 'Deluxe',   3, 2500),
  (2, '201', 'Suite',    4, 7000);

-- ---------------------------------------------------------------
-- SEARCH: available rooms in a city for given dates
-- Replace the values (or use parameters :city etc. in your backend)
-- ---------------------------------------------------------------
SELECT h.name AS hotel, r.room_id, r.room_number, r.room_type, r.price_per_night
FROM rooms r
JOIN hotels h ON h.hotel_id = r.hotel_id
WHERE h.city = 'Tirupati'
  AND r.max_guests >= 2
  AND NOT EXISTS (
      SELECT 1 FROM bookings b
      WHERE b.room_id = r.room_id
        AND b.status <> 'cancelled'
        AND b.check_in  < '2026-11-05'   -- requested check-out
        AND b.check_out > '2026-11-01'   -- requested check-in
  )
ORDER BY r.price_per_night;

-- ---------------------------------------------------------------
-- CREATE A BOOKING (total = nights x price)
-- ---------------------------------------------------------------
INSERT INTO bookings (user_id, room_id, check_in, check_out, guests, total_price)
SELECT 1, r.room_id, '2026-11-01', '2026-11-05', 2,
       ('2026-11-05'::date - '2026-11-01'::date) * r.price_per_night
FROM rooms r WHERE r.room_id = 1;

-- ---------------------------------------------------------------
-- MY BOOKINGS
-- ---------------------------------------------------------------
SELECT b.booking_id, h.name AS hotel, r.room_number, r.room_type,
       b.check_in, b.check_out, b.total_price, b.status
FROM bookings b
JOIN rooms r  ON r.room_id  = b.room_id
JOIN hotels h ON h.hotel_id = r.hotel_id
WHERE b.user_id = 1
ORDER BY b.check_in DESC;

-- ---------------------------------------------------------------
-- CANCEL A BOOKING
-- ---------------------------------------------------------------
UPDATE bookings SET status = 'cancelled'
WHERE booking_id = 1 AND user_id = 1;
