-- Run on existing library_users DB if users table was created without password_hash
ALTER TABLE users ADD COLUMN password_hash VARCHAR(255) NULL AFTER is_blocked;
