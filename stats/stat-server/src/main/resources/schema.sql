DROP TABLE IF EXISTS stats;

CREATE TABLE IF NOT EXISTS stats (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    app VARCHAR(50) NOT NULL,
    uri VARCHAR(150) NOT NULL,
    ip VARCHAR(15),
    timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_stats PRIMARY KEY (id)
);