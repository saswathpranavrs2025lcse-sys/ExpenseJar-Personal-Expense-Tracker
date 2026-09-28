-- ============================================================
-- Expense Tracker — MySQL Database Setup Script
-- ============================================================

-- 1. Create the database if it doesn't already exist
CREATE DATABASE IF NOT EXISTS expense_tracker
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE expense_tracker;

-- 2. Optional: Create dedicated application user (if not using root)
-- CREATE USER IF NOT EXISTS 'expense_user'@'localhost' IDENTIFIED BY 'expense_password';
-- GRANT ALL PRIVILEGES ON expense_tracker.* TO 'expense_user'@'localhost';
-- FLUSH PRIVILEGES;

-- 3. Verify database creation
SELECT DATABASE() AS current_database;
