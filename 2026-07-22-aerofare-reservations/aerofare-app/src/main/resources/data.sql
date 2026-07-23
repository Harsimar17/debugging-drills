-- ===================== Airports (PK = IATA code) =====================
INSERT INTO airport (iata_code, name, city, country, zone_id) VALUES ('JFK', 'John F. Kennedy Intl', 'New York',    'USA', 'America/New_York');
INSERT INTO airport (iata_code, name, city, country, zone_id) VALUES ('LAX', 'Los Angeles Intl',     'Los Angeles', 'USA', 'America/Los_Angeles');
INSERT INTO airport (iata_code, name, city, country, zone_id) VALUES ('SFO', 'San Francisco Intl',   'San Francisco','USA','America/Los_Angeles');
INSERT INTO airport (iata_code, name, city, country, zone_id) VALUES ('ORD', 'O''Hare Intl',         'Chicago',     'USA', 'America/Chicago');
INSERT INTO airport (iata_code, name, city, country, zone_id) VALUES ('LHR', 'Heathrow',             'London',      'UK',  'Europe/London');
INSERT INTO airport (iata_code, name, city, country, zone_id) VALUES ('HND', 'Haneda',               'Tokyo',       'Japan','Asia/Tokyo');

-- ===================== Aircraft (identity ids 1,2) =====================
INSERT INTO aircraft (registration, model, economy_seats, premium_economy_seats, business_seats, first_seats)
VALUES ('N320AA', 'Airbus A320', 150, 0, 20, 0);
INSERT INTO aircraft (registration, model, economy_seats, premium_economy_seats, business_seats, first_seats)
VALUES ('N777BB', 'Boeing 777',  250, 40, 40, 8);

-- ===================== Flights (identity ids 1..5) =====================
-- departure_time is in the ORIGIN airport local time; arrival_time in the DESTINATION airport local time.
INSERT INTO flight (flight_number, origin_code, destination_code, aircraft_id, departure_time, arrival_time, status, base_fare)
VALUES ('FL100', 'JFK', 'LAX', 1, TIMESTAMP '2026-08-01 08:00:00', TIMESTAMP '2026-08-01 11:00:00', 'SCHEDULED', 300.00);
INSERT INTO flight (flight_number, origin_code, destination_code, aircraft_id, departure_time, arrival_time, status, base_fare)
VALUES ('FL200', 'JFK', 'LAX', 1, TIMESTAMP '2026-08-01 15:00:00', TIMESTAMP '2026-08-01 18:00:00', 'SCHEDULED', 320.00);
INSERT INTO flight (flight_number, origin_code, destination_code, aircraft_id, departure_time, arrival_time, status, base_fare)
VALUES ('FL300', 'JFK', 'LHR', 2, TIMESTAMP '2026-08-01 20:00:00', TIMESTAMP '2026-08-02 08:00:00', 'SCHEDULED', 700.00);
INSERT INTO flight (flight_number, origin_code, destination_code, aircraft_id, departure_time, arrival_time, status, base_fare)
VALUES ('FL400', 'HND', 'LAX', 2, TIMESTAMP '2026-08-01 17:00:00', TIMESTAMP '2026-08-01 10:00:00', 'SCHEDULED', 900.00);
INSERT INTO flight (flight_number, origin_code, destination_code, aircraft_id, departure_time, arrival_time, status, base_fare)
VALUES ('FL500', 'SFO', 'ORD', 1, TIMESTAMP '2026-08-02 09:00:00', TIMESTAMP '2026-08-02 15:00:00', 'SCHEDULED', 250.00);

-- ===================== Seat inventory =====================
-- FL100 (flight_id 1): plenty
INSERT INTO seat_inventory (flight_id, cabin_class, total_seats, booked_seats, version) VALUES (1, 'ECONOMY', 150, 10, 0);
INSERT INTO seat_inventory (flight_id, cabin_class, total_seats, booked_seats, version) VALUES (1, 'BUSINESS', 20, 2, 0);
-- FL200 (flight_id 2): exactly one economy seat left
INSERT INTO seat_inventory (flight_id, cabin_class, total_seats, booked_seats, version) VALUES (2, 'ECONOMY', 150, 149, 0);
INSERT INTO seat_inventory (flight_id, cabin_class, total_seats, booked_seats, version) VALUES (2, 'BUSINESS', 20, 5, 0);
-- FL300 (flight_id 3): long-haul widebody
INSERT INTO seat_inventory (flight_id, cabin_class, total_seats, booked_seats, version) VALUES (3, 'ECONOMY', 250, 60, 0);
INSERT INTO seat_inventory (flight_id, cabin_class, total_seats, booked_seats, version) VALUES (3, 'PREMIUM_ECONOMY', 40, 5, 0);
INSERT INTO seat_inventory (flight_id, cabin_class, total_seats, booked_seats, version) VALUES (3, 'BUSINESS', 40, 8, 0);
INSERT INTO seat_inventory (flight_id, cabin_class, total_seats, booked_seats, version) VALUES (3, 'FIRST', 8, 1, 0);
-- FL400 (flight_id 4): trans-pacific
INSERT INTO seat_inventory (flight_id, cabin_class, total_seats, booked_seats, version) VALUES (4, 'ECONOMY', 250, 40, 0);
INSERT INTO seat_inventory (flight_id, cabin_class, total_seats, booked_seats, version) VALUES (4, 'BUSINESS', 40, 4, 0);
INSERT INTO seat_inventory (flight_id, cabin_class, total_seats, booked_seats, version) VALUES (4, 'FIRST', 8, 0, 0);
-- FL500 (flight_id 5): domestic
INSERT INTO seat_inventory (flight_id, cabin_class, total_seats, booked_seats, version) VALUES (5, 'ECONOMY', 150, 20, 0);
INSERT INTO seat_inventory (flight_id, cabin_class, total_seats, booked_seats, version) VALUES (5, 'BUSINESS', 20, 1, 0);

-- ===================== Fare rules =====================
-- Base multipliers per cabin (ADULT), plus economy passenger-type variants.
INSERT INTO fare_rule (cabin_class, passenger_type, fare_multiplier, refundable, change_fee_percent, cancellation_fee_percent)
VALUES ('ECONOMY', 'ADULT', 1.000, FALSE, 15.00, 25.00);
INSERT INTO fare_rule (cabin_class, passenger_type, fare_multiplier, refundable, change_fee_percent, cancellation_fee_percent)
VALUES ('ECONOMY', 'CHILD', 0.750, FALSE, 15.00, 25.00);
INSERT INTO fare_rule (cabin_class, passenger_type, fare_multiplier, refundable, change_fee_percent, cancellation_fee_percent)
VALUES ('ECONOMY', 'INFANT', 0.100, FALSE, 0.00, 0.00);
INSERT INTO fare_rule (cabin_class, passenger_type, fare_multiplier, refundable, change_fee_percent, cancellation_fee_percent)
VALUES ('ECONOMY', 'SENIOR', 0.900, FALSE, 15.00, 25.00);
INSERT INTO fare_rule (cabin_class, passenger_type, fare_multiplier, refundable, change_fee_percent, cancellation_fee_percent)
VALUES ('PREMIUM_ECONOMY', 'ADULT', 1.500, FALSE, 12.00, 20.00);
INSERT INTO fare_rule (cabin_class, passenger_type, fare_multiplier, refundable, change_fee_percent, cancellation_fee_percent)
VALUES ('BUSINESS', 'ADULT', 2.500, TRUE, 8.00, 10.00);
INSERT INTO fare_rule (cabin_class, passenger_type, fare_multiplier, refundable, change_fee_percent, cancellation_fee_percent)
VALUES ('FIRST', 'ADULT', 4.000, TRUE, 5.00, 8.00);
