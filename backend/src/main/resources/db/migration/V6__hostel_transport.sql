-- V6: Hostel + Transport modules (scoped MVP)
-- Adds studentType to student_profiles, hostel block/room/allocation/gate-pass,
-- and bus route/stop/pass entities with demo seed data.

ALTER TABLE student_profiles
    ADD COLUMN student_type VARCHAR(20) DEFAULT 'DAY_SCHOLAR'
        COMMENT 'HOSTELLER | DAY_SCHOLAR';

-- Hostel -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS hostel_blocks (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    block_name     VARCHAR(50)  NOT NULL,
    total_rooms    INT          NOT NULL DEFAULT 0,
    occupied_rooms INT          NOT NULL DEFAULT 0,
    warden_name    VARCHAR(100),
    warden_contact VARCHAR(20),
    gender         VARCHAR(10)  DEFAULT 'MIXED' COMMENT 'MALE | FEMALE | MIXED'
);

CREATE TABLE IF NOT EXISTS hostel_rooms (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    block_id    BIGINT      NOT NULL,
    room_number VARCHAR(20) NOT NULL,
    capacity    INT         NOT NULL DEFAULT 2,
    occupied    INT         NOT NULL DEFAULT 0,
    amenities   VARCHAR(500),
    FOREIGN KEY (block_id) REFERENCES hostel_blocks(id)
);

CREATE TABLE IF NOT EXISTS hostel_allocations (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    room_id    BIGINT NOT NULL,
    from_date  DATE   NOT NULL,
    to_date    DATE,
    status     VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE | VACATED',
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (room_id)    REFERENCES hostel_rooms(id)
);

CREATE TABLE IF NOT EXISTS gate_pass_requests (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id                  BIGINT      NOT NULL,
    purpose                     TEXT        NOT NULL,
    out_date_time               DATETIME    NOT NULL,
    expected_return_date_time   DATETIME    NOT NULL,
    destination                 VARCHAR(200),
    status                      VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        COMMENT 'PENDING | APPROVED | REJECTED',
    approved_by                 BIGINT,
    assigned_to_role            VARCHAR(20) DEFAULT 'ADMIN',
    created_at                  DATETIME    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id)  REFERENCES users(id),
    FOREIGN KEY (approved_by) REFERENCES users(id)
);

-- Transport ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bus_routes (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_number   VARCHAR(20)  NOT NULL,
    route_name     VARCHAR(150) NOT NULL,
    start_point    VARCHAR(100) NOT NULL,
    end_point      VARCHAR(100) NOT NULL,
    departure_time VARCHAR(20),
    return_time    VARCHAR(20),
    driver_name    VARCHAR(100),
    driver_contact VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS bus_stops (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_id     BIGINT      NOT NULL,
    stop_name    VARCHAR(100) NOT NULL,
    stop_order   INT          NOT NULL,
    arrival_time VARCHAR(20),
    FOREIGN KEY (route_id) REFERENCES bus_routes(id)
);

CREATE TABLE IF NOT EXISTS student_bus_passes (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id       BIGINT NOT NULL,
    route_id         BIGINT NOT NULL,
    boarding_stop_id BIGINT,
    valid_from       DATE   NOT NULL,
    valid_to         DATE   NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        COMMENT 'ACTIVE | EXPIRED | CANCELLED',
    FOREIGN KEY (student_id)       REFERENCES users(id),
    FOREIGN KEY (route_id)         REFERENCES bus_routes(id),
    FOREIGN KEY (boarding_stop_id) REFERENCES bus_stops(id)
);

-- Seed demo data -----------------------------------------------------------
INSERT IGNORE INTO hostel_blocks (id, block_name, total_rooms, occupied_rooms, warden_name, warden_contact, gender)
VALUES
    (1, 'Block A (Boys)',  50, 35, 'Dr. R. Krishnamurthy', '9876543210', 'MALE'),
    (2, 'Block B (Girls)', 40, 28, 'Dr. S. Priya',        '9876543211', 'FEMALE');

INSERT IGNORE INTO hostel_rooms (id, block_id, room_number, capacity, occupied, amenities)
VALUES
    (1, 1, 'A-101', 2, 2, 'AC, WiFi, Attached Bathroom'),
    (2, 1, 'A-102', 2, 1, 'WiFi, Common Bathroom'),
    (3, 2, 'B-201', 3, 3, 'AC, WiFi, Attached Bathroom');

INSERT IGNORE INTO bus_routes (id, route_number, route_name, start_point, end_point, departure_time, return_time, driver_name, driver_contact)
VALUES
    (1, 'R-01', 'Anna Nagar Route', 'College Gate', 'Anna Nagar West', '08:00 AM', '05:30 PM', 'K. Murugan', '9876540001'),
    (2, 'R-02', 'Tambaram Route',   'College Gate', 'Tambaram Bus Stand', '07:45 AM', '05:45 PM', 'S. Kumar',  '9876540002');

INSERT IGNORE INTO bus_stops (id, route_id, stop_name, stop_order, arrival_time)
VALUES
    (1, 1, 'College Gate',       1, '08:00 AM'),
    (2, 1, 'Koyambedu',          2, '08:25 AM'),
    (3, 1, 'Anna Nagar West',    3, '08:45 AM'),
    (4, 2, 'College Gate',       1, '07:45 AM'),
    (5, 2, 'Chromepet',          2, '08:10 AM'),
    (6, 2, 'Tambaram Bus Stand', 3, '08:30 AM');
