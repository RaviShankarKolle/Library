CREATE TABLE IF NOT EXISTS fines (
    id            BIGINT         NOT NULL AUTO_INCREMENT,
    lending_id    BIGINT         NOT NULL,
    user_id       BIGINT         NOT NULL,
    amount        DECIMAL(12, 2) NOT NULL,
    status        VARCHAR(32)    NOT NULL,
    reason        VARCHAR(64)    NOT NULL,
    accrual_date  DATE           NOT NULL,
    created_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at       TIMESTAMP      NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_fine_lending_day (lending_id, accrual_date),
    KEY idx_fine_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
