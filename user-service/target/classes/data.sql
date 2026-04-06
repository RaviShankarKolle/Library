-- Demo logins (password for both: "password") — from Spring BCryptPasswordEncoder test vector
INSERT IGNORE INTO users (email, role, max_limit, current_count, is_blocked, password_hash) VALUES
('librarian@library.local', 'LIBRARIAN', 100, 0, 0, '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG5YGyJKxZjB5n5f2m'),
('patron@library.local', 'PATRON', 5, 0, 0, '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG5YGyJKxZjB5n5f2m');
