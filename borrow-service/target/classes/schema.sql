CREATE TABLE IF NOT EXISTS borrow_records (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    user_id      BIGINT       NOT NULL,
    copy_id      BIGINT       NOT NULL,
    barcode      VARCHAR(64)  NOT NULL,
    status       VARCHAR(32)  NOT NULL,
    loan_days    INT          NOT NULL DEFAULT 14,
    borrow_date  TIMESTAMP    NULL,
    due_date     TIMESTAMP    NULL,
    returned_at  TIMESTAMP    NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_borrow_user_status (user_id, status),
    KEY idx_borrow_copy (copy_id),
    KEY idx_borrow_overdue (status, due_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
