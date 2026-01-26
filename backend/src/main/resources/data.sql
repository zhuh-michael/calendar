-- StarQuest Test Data
-- This file is executed automatically by Spring Boot after schema creation

-- Default admin user
INSERT OR IGNORE INTO users (username, password, role, star_balance, nickname)
VALUES ('admin', '$2a$10$8K2pz5G2XcJvQY6QKZWxNe.3Q2vz5G2XcJvQY6QKZWxNe.3Q2vz5G', 'PARENT', 0, '管理员');

-- Test kid user
INSERT OR IGNORE INTO users (username, password, role, star_balance, avatar, nickname)
VALUES ('testkid', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'KID', 50, '', '测试小朋友');

-- Test kid 2
INSERT OR IGNORE INTO users (username, password, role, star_balance, avatar, nickname)
VALUES ('xiaoming', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'KID', 25, '', '小明');

-- Test kid 3
INSERT OR IGNORE INTO users (username, password, role, star_balance, avatar, nickname)
VALUES ('xiaohong', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'KID', 35, '', '小红');

-- Task templates (寒假作息模板)
INSERT OR IGNORE INTO tasks (title, kid_id, start_time, status, reward_stars, is_template, needs_review, description)
VALUES
('08:00 晨读', 1, '2024-01-15 08:00:00', 0, 5, TRUE, FALSE, '晨读30分钟'),
('08:30 早餐', 1, '2024-01-15 08:30:00', 0, 3, TRUE, FALSE, '吃早餐'),
('09:00 写作业', 1, '2024-01-15 09:00:00', 0, 10, TRUE, TRUE, '完成家庭作业'),
('12:00 午餐', 1, '2024-01-15 12:00:00', 0, 3, TRUE, FALSE, '吃午餐'),
('14:00 阅读', 1, '2024-01-15 14:00:00', 0, 8, TRUE, FALSE, '阅读课外书'),
('18:00 晚餐', 1, '2024-01-15 18:00:00', 0, 3, TRUE, FALSE, '吃晚餐'),
('20:00 复习', 1, '2024-01-15 20:00:00', 0, 8, TRUE, TRUE, '复习当日功课'),
('21:00 收拾书包', 1, '2024-01-15 21:00:00', 0, 5, TRUE, FALSE, '为明天上学做准备');

-- Sample tasks for test kids
INSERT OR IGNORE INTO tasks (title, kid_id, start_time, status, reward_stars, is_template, needs_review, description)
VALUES
('完成数学作业', 2, '2024-01-15 14:00:00', 0, 10, FALSE, TRUE, '完成第5章练习题'),
('晨读30分钟', 2, '2024-01-15 08:00:00', 2, 5, FALSE, FALSE, '阅读英文课文'),
('整理书包', 2, '2024-01-15 21:00:00', 2, 5, FALSE, FALSE, '为明天上学做准备'),
('完成语文作业', 3, '2024-01-15 15:00:00', 0, 8, FALSE, FALSE, '抄写生字20个');

-- Sample rewards (商品)
INSERT OR IGNORE INTO rewards (name, cost, stock, type, description, image_url, active)
VALUES
('乐高积木', 50, 3, 'ITEM', '创意建筑玩具', '', TRUE),
('看电视券', 20, 10, 'VIRTUAL', '30分钟电视时间', '', TRUE),
('幸运盲盒券', 15, 20, 'BLINDBOX', '抽奖必备道具', '', TRUE),
('巧克力', 8, 15, 'ITEM', '美味的黑巧克力', '', TRUE),
('阅读券', 12, 25, 'VIRTUAL', '1小时阅读时间', '', TRUE);

-- Sample transactions (交易记录)
INSERT OR IGNORE INTO transactions (user_id, amount, reason, related_task_id)
VALUES
(2, 5, '完成任务: 晨读30分钟', 10),
(2, 5, '完成任务: 整理书包', 11),
(3, 8, '完成任务: 完成语文作业', 12);

-- Sample mood logs (心情记录)
INSERT OR IGNORE INTO mood_logs (kid_id, mood_type, note)
VALUES
(2, 'HAPPY', '今天作业完成得很好！'),
(3, 'NEUTRAL', '一般般啦'),
(2, 'SAD', '作业有点难');

-- Sample orders (订单记录)
INSERT OR IGNORE INTO orders (kid_id, reward_id, status, create_time)
VALUES
(2, 1, 0, '2024-01-15 16:00:00'),  -- 待兑换乐高积木
(3, 2, 1, '2024-01-14 18:00:00');  -- 已兑换看电视券

