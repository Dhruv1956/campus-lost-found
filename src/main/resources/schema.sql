-- Database schema for the Campus Lost and Found Portal.
-- Executed automatically at startup by AppStartupListener (CREATE TABLE IF NOT EXISTS is safe to re-run).
-- Written in standard SQL so it works on both H2 (MySQL mode) and MySQL.

CREATE TABLE IF NOT EXISTS users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(120) NOT NULL UNIQUE,
    phone         VARCHAR(20),
    salt          VARCHAR(64)  NOT NULL,
    password_hash VARCHAR(128) NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS items (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT NOT NULL,
    type        VARCHAR(10)  NOT NULL,
    title       VARCHAR(120) NOT NULL,
    description VARCHAR(1000),
    category    VARCHAR(40)  NOT NULL,
    location    VARCHAR(120) NOT NULL,
    event_date  DATE NOT NULL,
    status      VARCHAR(10)  NOT NULL DEFAULT 'OPEN',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS claims (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    item_id    INT NOT NULL,
    claimer_id INT NOT NULL,
    message    VARCHAR(500) NOT NULL,
    status     VARCHAR(10) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id)    REFERENCES items(id),
    FOREIGN KEY (claimer_id) REFERENCES users(id)
);
