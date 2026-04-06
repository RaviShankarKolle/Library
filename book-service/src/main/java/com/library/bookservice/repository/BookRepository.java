package com.library.bookservice.repository;

import com.library.bookservice.domain.Book;
import com.library.bookservice.domain.BookCopy;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Optional;

@Repository
public class BookRepository {

    private static final RowMapper<BookCopy> COPY_ROW = (rs, i) -> {
        BookCopy c = new BookCopy();
        c.setId(rs.getLong("id"));
        c.setBookId(rs.getLong("book_id"));
        c.setBarcode(rs.getString("barcode"));
        c.setStatus(rs.getString("status"));
        Timestamp cr = rs.getTimestamp("created_at");
        Timestamp up = rs.getTimestamp("updated_at");
        if (cr != null) {
            c.setCreatedAt(cr.toInstant());
        }
        if (up != null) {
            c.setUpdatedAt(up.toInstant());
        }
        return c;
    };

    private final JdbcTemplate jdbcTemplate;

    public BookRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Book insertBook(String isbn, String title, String author) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO books (isbn, title, author) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, isbn);
            ps.setString(2, title);
            ps.setString(3, author);
            return ps;
        }, kh);
        Number key = kh.getKey();
        if (key == null) {
            throw new IllegalStateException("No key for book insert");
        }
        return findBookById(key.longValue()).orElseThrow();
    }

    public Optional<Book> findBookById(long id) {
        try {
            Book b = jdbcTemplate.queryForObject(
                    "SELECT id, isbn, title, author, created_at FROM books WHERE id = ?",
                    (rs, row) -> {
                        Book x = new Book();
                        x.setId(rs.getLong("id"));
                        x.setIsbn(rs.getString("isbn"));
                        x.setTitle(rs.getString("title"));
                        x.setAuthor(rs.getString("author"));
                        Timestamp c = rs.getTimestamp("created_at");
                        if (c != null) {
                            x.setCreatedAt(c.toInstant());
                        }
                        return x;
                    },
                    id);
            return Optional.ofNullable(b);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public BookCopy insertCopy(long bookId, String barcode, String status) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO book_copies (book_id, barcode, status) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, bookId);
            ps.setString(2, barcode);
            ps.setString(3, status);
            return ps;
        }, kh);
        Number key = kh.getKey();
        if (key == null) {
            throw new IllegalStateException("No key for copy insert");
        }
        return findCopyById(key.longValue()).orElseThrow();
    }

    public Optional<BookCopy> findCopyById(long copyId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT id, book_id, barcode, status, created_at, updated_at FROM book_copies WHERE id = ?",
                    COPY_ROW,
                    copyId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<BookCopy> findCopyByBarcode(String barcode) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT id, book_id, barcode, status, created_at, updated_at FROM book_copies WHERE barcode = ?",
                    COPY_ROW,
                    barcode));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /** AVAILABLE -> RESERVED, returns rows updated (0 or 1). */
    public int reserveIfAvailable(String barcode) {
        return jdbcTemplate.update(
                "UPDATE book_copies SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE barcode = ? AND status = ?",
                BookCopy.RESERVED,
                barcode,
                BookCopy.AVAILABLE);
    }

    /** RESERVED -> ISSUED */
    public int markIssued(long copyId) {
        return jdbcTemplate.update(
                "UPDATE book_copies SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND status = ?",
                BookCopy.ISSUED,
                copyId,
                BookCopy.RESERVED);
    }

    /** ISSUED -> AVAILABLE */
    public int markReturned(long copyId) {
        return jdbcTemplate.update(
                "UPDATE book_copies SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND status = ?",
                BookCopy.AVAILABLE,
                copyId,
                BookCopy.ISSUED);
    }

    /** RESERVED -> AVAILABLE (rollback after failed borrow insert). */
    public int releaseReservation(String barcode) {
        return jdbcTemplate.update(
                "UPDATE book_copies SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE barcode = ? AND status = ?",
                BookCopy.AVAILABLE,
                barcode,
                BookCopy.RESERVED);
    }
}
