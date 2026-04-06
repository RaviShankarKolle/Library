package com.library.borrowservice.repository;

import com.library.borrowservice.domain.BorrowRecord;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class BorrowRepository {

    private static final RowMapper<BorrowRecord> ROW = (rs, i) -> {
        BorrowRecord b = new BorrowRecord();
        b.setId(rs.getLong("id"));
        b.setUserId(rs.getLong("user_id"));
        b.setCopyId(rs.getLong("copy_id"));
        b.setBarcode(rs.getString("barcode"));
        b.setStatus(rs.getString("status"));
        b.setLoanDays(rs.getInt("loan_days"));
        Timestamp bd = rs.getTimestamp("borrow_date");
        Timestamp dd = rs.getTimestamp("due_date");
        Timestamp rt = rs.getTimestamp("returned_at");
        Timestamp c = rs.getTimestamp("created_at");
        Timestamp u = rs.getTimestamp("updated_at");
        if (bd != null) {
            b.setBorrowDate(bd.toInstant());
        }
        if (dd != null) {
            b.setDueDate(dd.toInstant());
        }
        if (rt != null) {
            b.setReturnedAt(rt.toInstant());
        }
        if (c != null) {
            b.setCreatedAt(c.toInstant());
        }
        if (u != null) {
            b.setUpdatedAt(u.toInstant());
        }
        return b;
    };

    private final JdbcTemplate jdbcTemplate;

    public BorrowRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<BorrowRecord> findById(long id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT id, user_id, copy_id, barcode, status, loan_days, borrow_date, due_date, returned_at, created_at, updated_at "
                            + "FROM borrow_records WHERE id = ?",
                    ROW,
                    id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public BorrowRecord insertReserved(long userId, long copyId, String barcode, int loanDays) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO borrow_records (user_id, copy_id, barcode, status, loan_days) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setLong(2, copyId);
            ps.setString(3, barcode);
            ps.setString(4, BorrowRecord.RESERVED);
            ps.setInt(5, loanDays);
            return ps;
        }, kh);
        Number key = kh.getKey();
        if (key == null) {
            throw new IllegalStateException("No key for borrow insert");
        }
        return findById(key.longValue()).orElseThrow();
    }

    public int markAllocated(long borrowId, Instant borrowDate, Instant dueDate) {
        return jdbcTemplate.update(
                "UPDATE borrow_records SET status = ?, borrow_date = ?, due_date = ?, updated_at = CURRENT_TIMESTAMP "
                        + "WHERE id = ? AND status = ?",
                BorrowRecord.ALLOCATED,
                Timestamp.from(borrowDate),
                Timestamp.from(dueDate),
                borrowId,
                BorrowRecord.RESERVED);
    }

    public int markReturned(long borrowId, Instant returnedAt) {
        return jdbcTemplate.update(
                "UPDATE borrow_records SET status = ?, returned_at = ?, updated_at = CURRENT_TIMESTAMP "
                        + "WHERE id = ? AND status = ?",
                BorrowRecord.RETURNED,
                Timestamp.from(returnedAt),
                borrowId,
                BorrowRecord.ALLOCATED);
    }

    public List<BorrowRecord> findOverdue(Instant now) {
        return jdbcTemplate.query(
                "SELECT id, user_id, copy_id, barcode, status, loan_days, borrow_date, due_date, returned_at, created_at, updated_at "
                        + "FROM borrow_records WHERE status = ? AND due_date IS NOT NULL AND due_date < ?",
                ROW,
                BorrowRecord.ALLOCATED,
                Timestamp.from(now));
    }
}
