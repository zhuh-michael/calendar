-- StarQuest Database Schema
-- This file is executed automatically by Spring Boot on startup

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    star_balance INTEGER DEFAULT 0,
    avatar VARCHAR(255),
    nickname VARCHAR(50)
);

-- Tasks table
CREATE TABLE IF NOT EXISTS tasks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title VARCHAR(255) NOT NULL,
    kid_id INTEGER NOT NULL,
    start_time DATETIME,
    status INTEGER DEFAULT 0,
    reward_stars INTEGER NOT NULL,
    is_template BOOLEAN DEFAULT FALSE,
    needs_review BOOLEAN DEFAULT FALSE,
    description TEXT,
    reject_reason TEXT,
    FOREIGN KEY (kid_id) REFERENCES users(id)
);

-- Rewards table
CREATE TABLE IF NOT EXISTS rewards (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(255) NOT NULL,
    cost INTEGER NOT NULL,
    stock INTEGER DEFAULT 0,
    type VARCHAR(20) DEFAULT 'ITEM',
    description TEXT,
    image_url VARCHAR(500),
    active BOOLEAN DEFAULT TRUE
);

-- Orders table
CREATE TABLE IF NOT EXISTS orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    kid_id INTEGER NOT NULL,
    reward_id INTEGER NOT NULL,
    status INTEGER DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME,
    FOREIGN KEY (kid_id) REFERENCES users(id),
    FOREIGN KEY (reward_id) REFERENCES rewards(id)
);

-- Transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    amount INTEGER NOT NULL,
    reason VARCHAR(255) NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    related_task_id INTEGER,
    related_reward_id INTEGER,
    related_order_id INTEGER,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (related_task_id) REFERENCES tasks(id),
    FOREIGN KEY (related_reward_id) REFERENCES rewards(id),
    FOREIGN KEY (related_order_id) REFERENCES orders(id)
);

-- Mood logs table
CREATE TABLE IF NOT EXISTS mood_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    kid_id INTEGER NOT NULL,
    mood_type VARCHAR(20) NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    note TEXT,
    FOREIGN KEY (kid_id) REFERENCES users(id)
);

-- Task evidence table
CREATE TABLE IF NOT EXISTS task_evidence (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    task_id INTEGER NOT NULL,
    image_path VARCHAR(500) NOT NULL,
    upload_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
);

-- Lucky draw records table
CREATE TABLE IF NOT EXISTS lucky_draw_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    kid_id INTEGER NOT NULL,
    result_type VARCHAR(20) NOT NULL,
    stars_earned INTEGER NOT NULL,
    reward_description VARCHAR(255) NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (kid_id) REFERENCES users(id)
);
