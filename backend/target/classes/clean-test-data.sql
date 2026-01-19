-- Clean Test Data Script
-- Execute this script to remove all test data before production deployment

-- Remove test users (keep admin)
DELETE FROM transactions WHERE user_id IN (SELECT id FROM users WHERE username IN ('testkid', 'xiaoming', 'xiaohong'));
DELETE FROM mood_logs WHERE kid_id IN (SELECT id FROM users WHERE username IN ('testkid', 'xiaoming', 'xiaohong'));
DELETE FROM lucky_draw_records WHERE kid_id IN (SELECT id FROM users WHERE username IN ('testkid', 'xiaoming', 'xiaohong'));
DELETE FROM orders WHERE kid_id IN (SELECT id FROM users WHERE username IN ('testkid', 'xiaoming', 'xiaohong'));
DELETE FROM tasks WHERE kid_id IN (SELECT id FROM users WHERE username IN ('testkid', 'xiaoming', 'xiaohong'));
DELETE FROM users WHERE username IN ('testkid', 'xiaoming', 'xiaohong');

-- Reset auto-increment counters
DELETE FROM sqlite_sequence WHERE name IN ('users', 'tasks', 'transactions', 'mood_logs', 'lucky_draw_records', 'orders');
