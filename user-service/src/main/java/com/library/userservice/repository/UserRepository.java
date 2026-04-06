package com.library.userservice.repository;

import com.library.userservice.domain.User;
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
public class UserRepository {

    private static final RowMapper<User> ROW_MAPPER = (rs, rowNum) -> {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setEmail(rs.getString("email"));
        u.setRole(rs.getString("role"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setMaxLimit(rs.getInt("max_limit"));
        u.setCurrentCount(rs.getInt("current_count"));
        u.setBlocked(rs.getBoolean("is_blocked"));
        Timestamp c = rs.getTimestamp("created_at");
        Timestamp t = rs.getTimestamp("updated_at");
        if (c != null) {
            u.setCreatedAt(c.toInstant());
        }
        if (t != null) {
            u.setUpdatedAt(t.toInstant());
        }
        return u;
    };

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<User> findById(long id) {
        try {
            User u = jdbcTemplate.queryForObject(
                    "SELECT id, email, role, password_hash, max_limit, current_count, is_blocked, created_at, updated_at "
                            + "FROM users WHERE id = ?",
                    ROW_MAPPER,
                    id);
            return Optional.ofNullable(u);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public User insert(String email, String role, int maxLimit, String passwordHash) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO users (email, role, max_limit, current_count, is_blocked, password_hash) "
                            + "VALUES (?, ?, ?, 0, 0, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, email);
            ps.setString(2, role);
            ps.setInt(3, maxLimit);
            ps.setString(4, passwordHash);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Insert did not return id");
        }
        return findById(key.longValue()).orElseThrow();
    }

    /**
     * Increments borrow count only if below max_limit. Returns number of rows updated (0 or 1).
     */
    public int incrementBorrowCountIfAllowed(long userId) {
        return jdbcTemplate.update(
                "UPDATE users SET current_count = current_count + 1, updated_at = CURRENT_TIMESTAMP "
                        + "WHERE id = ? AND is_blocked = 0 AND current_count < max_limit",
                userId);
    }

    /**
     * Decrements borrow count but not below zero.
     */
    public int decrementBorrowCount(long userId) {
        return jdbcTemplate.update(
                "UPDATE users SET current_count = GREATEST(0, current_count - 1), updated_at = CURRENT_TIMESTAMP "
                        + "WHERE id = ?",
                userId);
    }
}
