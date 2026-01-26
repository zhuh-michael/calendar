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
    nickname VARCHAR(50),
    -- RPG 核心字段
    exp INTEGER DEFAULT 0 NOT NULL,-- '累计经验值(只增不减)'
    level INTEGER DEFAULT 1 NOT NULL, -- '当前等级'
    level_title VARCHAR(50) DEFAULT '星际见习生', -- '当前称号'
    streak_days INTEGER DEFAULT 0 NOT NULL, -- '连续打卡天数'
    last_checkin_date DATE DEFAULT NULL -- '最后一次打卡日期(用于判断断签)'
);

-- Tasks table
CREATE TABLE IF NOT EXISTS tasks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title VARCHAR(255) NOT NULL,
    kid_id INTEGER NOT NULL,
    start_time DATETIME,
    status INTEGER DEFAULT 0,
    reward_stars INTEGER NOT NULL,
    reward_xp INTEGER DEFAULT 0, -- '任务奖励的经验值'
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

-- RPG 等级配置表
CREATE TABLE IF NOT EXISTS level_config (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    level INTEGER NOT NULL UNIQUE, -- '等级'
    title VARCHAR(50) NOT NULL, -- '称号'
    required_exp INTEGER NOT NULL, -- '达到该等级所需累计XP'
    avatar_frame VARCHAR(100) DEFAULT NULL -- '解锁的头像框样式'
);

-- 初始化等级配置数据
INSERT OR IGNORE INTO level_config (level, title, required_exp, avatar_frame) VALUES
(1, '星际见习生', 0, 'bronze'),
(2, '领航员', 50, 'bronze'),
(3, '驾驶员', 150, 'bronze'),
(4, '小队长', 300, 'silver'),
(5, '探索者', 500, 'silver'),
(6, '先锋官', 800, 'silver'),
(7, '指挥官', 1200, 'gold'),
(8, '舰长', 1700, 'gold'),
(9, '提督', 2300, 'gold'),
(10, '宇宙领主', 3000, 'diamond');



-- 连签奖励配置表
CREATE TABLE IF NOT EXISTS streak_bonus_config (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    streak_days INTEGER NOT NULL UNIQUE, -- '连续天数'
    xp_bonus INTEGER NOT NULL, -- 'XP奖励'
    star_bonus INTEGER NOT NULL, -- '星星奖励'
    message VARCHAR(100) NOT NULL -- '前端提示文案'
);
-- 初始化连签奖励配置
INSERT OR IGNORE INTO streak_bonus_config (streak_days, xp_bonus, star_bonus, message) VALUES
(1, 5, 1, '好的开始！'),
(2, 5, 1, '继续保持！'),
(3, 10, 2, '渐入佳境！'),
(4, 10, 2, '坚持就是胜利！'),
(5, 10, 2, '你太棒了！'),
(6, 10, 2, '明天有大奖！'),
(7, 50, 10, '周冠王！奖励翻倍！');
