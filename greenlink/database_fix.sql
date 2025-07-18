-- Database Fix Script for GreenLink Points System
-- Run this script on your PostgreSQL database to fix the level column issues

-- Step 1: Add level column if it doesn't exist (without NOT NULL constraint)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'level') THEN
        ALTER TABLE users ADD COLUMN level INTEGER;
    END IF;
END $$;

-- Step 2: Update all existing users to have level 1
UPDATE users SET level = 1 WHERE level IS NULL;

-- Step 3: Now add the NOT NULL constraint and default value
ALTER TABLE users ALTER COLUMN level SET NOT NULL;
ALTER TABLE users ALTER COLUMN level SET DEFAULT 1;

-- Step 4: Create point_events table if it doesn't exist
CREATE TABLE IF NOT EXISTS point_events (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    points INTEGER NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    related_entity_id BIGINT,
    related_entity_type VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    old_level INTEGER,
    new_level INTEGER,
    leveled_up BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Step 5: Create indexes for point_events table
CREATE INDEX IF NOT EXISTS idx_point_events_user_id ON point_events(user_id);
CREATE INDEX IF NOT EXISTS idx_point_events_event_type ON point_events(event_type);
CREATE INDEX IF NOT EXISTS idx_point_events_created_at ON point_events(created_at);
CREATE INDEX IF NOT EXISTS idx_point_events_leveled_up ON point_events(leveled_up);

-- Step 6: Verify the changes
SELECT 'Database fix completed successfully!' as status;
SELECT COUNT(*) as total_users FROM users;
SELECT COUNT(*) as users_with_level FROM users WHERE level IS NOT NULL; 