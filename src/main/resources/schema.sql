CREATE TABLE IF NOT EXISTS event_record
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    type
    VARCHAR
(
    255
),
    payload TEXT,
    status VARCHAR
(
    50
),
    error_message TEXT,
    processed_at TIMESTAMP
    );