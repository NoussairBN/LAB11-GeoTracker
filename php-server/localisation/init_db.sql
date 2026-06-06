CREATE DATABASE IF NOT EXISTS localisation
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE localisation;

CREATE TABLE IF NOT EXISTS position (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    latitude      DOUBLE      NOT NULL,
    longitude     DOUBLE      NOT NULL,
    date_position DATETIME    NOT NULL,
    imei          VARCHAR(50) NOT NULL,
    created_at    TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Données de test
INSERT INTO position (latitude, longitude, date_position, imei) VALUES
    (48.8566,  2.3522, '2025-09-01 08:00:00', 'TEST-001'),
    (45.7640,  4.8357, '2025-09-01 09:00:00', 'TEST-001');
