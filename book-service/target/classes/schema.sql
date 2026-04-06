CREATE TABLE IF NOT EXISTS books (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    isbn       VARCHAR(32)  NULL,
    title      VARCHAR(512) NOT NULL,
    author     VARCHAR(512) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS book_copies (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    book_id    BIGINT       NOT NULL,
    barcode    VARCHAR(64)  NOT NULL,
    status     VARCHAR(32)  NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_book_copies_barcode (barcode),
    CONSTRAINT fk_book_copies_book FOREIGN KEY (book_id) REFERENCES books (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
