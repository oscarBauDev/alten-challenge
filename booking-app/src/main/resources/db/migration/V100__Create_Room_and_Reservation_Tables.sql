DROP TABLE IF EXISTS room CASCADE;
DROP TABLE IF EXISTS reservation CASCADE;

CREATE TABLE IF NOT EXISTS public.room (
    id serial PRIMARY KEY,
    number_of_beds SMALLINT NOT NULL,
    room_type VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.reservation (
    id SERIAL PRIMARY KEY,
    guest_name VARCHAR(255) NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    modified_at TIMESTAMP NOT NULL,
    cancelled BOOLEAN NOT NULL,
    room_id BIGINT REFERENCES room(id) ON DELETE CASCADE
);

INSERT INTO room (id, number_of_beds, room_type) VALUES (1, 1, 'STANDARD');

INSERT INTO reservation (check_in_date, check_out_date, created_at, modified_at, cancelled, guest_name, room_id) VALUES ('2023-04-15', '2023-04-16', '2023-03-25 12:00:00', '2023-03-25 12:00:00', false, 'Oscar Abril', 1);
INSERT INTO reservation (check_in_date, check_out_date, created_at, modified_at, cancelled, guest_name, room_id) VALUES ('2023-04-19', '2023-04-21', '2023-04-01 12:00:00', '2023-04-01 12:00:00', false, 'Charles Baudelaire', 1);
INSERT INTO reservation (check_in_date, check_out_date, created_at, modified_at, cancelled, guest_name, room_id) VALUES ('2023-04-17', '2023-04-18', '2023-04-16 12:00:00', '2023-04-16 12:00:00', false, 'Alten guest 1', 1);
INSERT INTO reservation (check_in_date, check_out_date, created_at, modified_at, cancelled, guest_name, room_id) VALUES ('2023-04-22', '2023-04-23', '2023-04-18 12:00:00', '2023-04-18 12:00:00', false, 'Alten guest 2', 1);