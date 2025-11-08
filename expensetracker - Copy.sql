CREATE DATABASE IF NOT EXISTS expensetracker;
USE expensetracker;

DROP TABLE IF EXISTS expenses;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;

-- Create simplified users table with plain text password
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(50) NOT NULL
);

CREATE TABLE categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE expenses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    amount DOUBLE NOT NULL,
    category VARCHAR(50) NOT NULL,
    date DATE NOT NULL,
    note VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Default categories
INSERT INTO categories (id, name, description) VALUES 
    (1, 'Food Meals and groceries'),
    (2, 'Transport Gas, public transport'),
    (3, 'Entertainment Movies, concerts');

-- Sample users with plain text passwords
INSERT INTO users (username, password) VALUES 
    ('testuser', 'testpass'),
    ('admin', 'adminpass');

SELECT 'Setup complete!' as Status;