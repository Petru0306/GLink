-- Check if admin user doesn't exist before inserting
INSERT INTO users (email, password, first_name, last_name, enabled, active, role, created_at, points)
SELECT 'admin@greenlink.com', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'Admin', 'User', true, true, 'ADMIN', CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@greenlink.com'
);

-- Insert default challenges if they don't exist
INSERT INTO challenges (title, description, points, type, status, start_date, end_date, updated_at, progress_percentage)
SELECT 'Reduce Water Usage', 'Take shorter showers and turn off the tap while brushing teeth to save water.', 50, 'DAILY', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '1 day', CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'Reduce Water Usage'
);

INSERT INTO challenges (title, description, points, type, status, start_date, end_date, updated_at, progress_percentage)
SELECT 'Zero Waste Week', 'Avoid single-use plastics and properly recycle all materials for one week.', 200, 'WEEKLY', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '7 days', CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'Zero Waste Week'
);

INSERT INTO challenges (title, description, points, type, status, start_date, end_date, updated_at, progress_percentage)
SELECT 'Plant a Garden', 'Start and maintain a small herb or vegetable garden for sustainable food production.', 500, 'MONTHLY', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '30 days', CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'Plant a Garden'
);

INSERT INTO challenges (title, description, points, type, status, start_date, end_date, updated_at, progress_percentage)
SELECT 'Community Clean-up', 'Organize and participate in a local community clean-up event.', 1000, 'SPECIAL', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '90 days', CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'Community Clean-up'
); 