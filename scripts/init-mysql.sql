-- Run as MySQL root once (no Docker). Adjust passwords if needed.
CREATE DATABASE IF NOT EXISTS library_users CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS library_books CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS library_borrows CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS library_fines CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'library'@'%' IDENTIFIED BY 'library';
GRANT ALL PRIVILEGES ON library_users.* TO 'library'@'%';
GRANT ALL PRIVILEGES ON library_books.* TO 'library'@'%';
GRANT ALL PRIVILEGES ON library_borrows.* TO 'library'@'%';
GRANT ALL PRIVILEGES ON library_fines.* TO 'library'@'%';
FLUSH PRIVILEGES;
