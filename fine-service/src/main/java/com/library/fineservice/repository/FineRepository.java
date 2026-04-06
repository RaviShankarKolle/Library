package com.library.fineservice.repository;

import com.library.fineservice.domain.Fine;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class FineRepository {

    private static final RowMapper<Fine> ROW = (rs, i) -> {
        Fine f = new Fine();
        f.setId(rs.getLong("id"));
        f.setLendingId(rs.getLong("lending_id"));
        f.setUserId(rs.getLong("user_id"));
        f.setAmount(rs.getBigDecimal("amount"));
        f.setStatus(rs.getString("status"));
        f.setReason(rs.getString("reason"));
        Date ad = rs.getDate("accrual_date");
        if (ad != null) {
            f.setAccrualDate(ad.toLocalDate());
        }
        var paid = rs.getTimestamp("paid_at");
        if (paid != null) {
            f.setPaidAt(paid.toInstant());
        }
        return f;
    };

    private final JdbcTemplate jdbcTemplate;

    public FineRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Idempotent daily accrual: INSERT IGNORE when (lending_id, accrual_date) already exists.
     *
     * @return 1 if inserted, 0 if duplicate
     */
    public int insertAccrualIfAbsent(long lendingId, long userId, BigDecimal amount, LocalDate accrualDate) {
        return jdbcTemplate.update(
                "INSERT IGNORE INTO fines (lending_id, user_id, amount, status, reason, accrual_date) "
                        + "VALUES (?, ?, ?, 'OPEN', 'OVERDUE_DAILY', ?)",
                lendingId,
                userId,
                amount,
                accrualDate);
    }

    public Optional<Fine> findById(long id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT id, lending_id, user_id, amount, status, reason, accrual_date, paid_at FROM fines WHERE id = ?",
                    ROW,
                    id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Fine> findByUserId(long userId) {
        return jdbcTemplate.query(
                "SELECT id, lending_id, user_id, amount, status, reason, accrual_date, paid_at FROM fines WHERE user_id = ? "
                        + "ORDER BY created_at DESC",
                ROW,
                userId);
    }

    public int markPaid(long fineId) {
        return jdbcTemplate.update(
                "UPDATE fines SET status = 'PAID', paid_at = CURRENT_TIMESTAMP WHERE id = ? AND status = 'OPEN'",
                fineId);
    }
}
