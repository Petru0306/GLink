-- Add level column to users table
ALTER TABLE users ADD COLUMN level INTEGER DEFAULT 1 NOT NULL;

-- Create point_events table
CREATE TABLE point_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
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

-- Create indexes for better performance
CREATE INDEX idx_point_events_user_id ON point_events(user_id);
CREATE INDEX idx_point_events_event_type ON point_events(event_type);
CREATE INDEX idx_point_events_created_at ON point_events(created_at);
CREATE INDEX idx_point_events_leveled_up ON point_events(leveled_up);

-- Update existing users to have proper level based on their current points
-- This uses the same level calculation logic as the Java code
UPDATE users 
SET level = CASE 
    WHEN points <= 50 THEN 1
    WHEN points <= 150 THEN 2
    WHEN points <= 300 THEN 3
    WHEN points <= 500 THEN 4
    WHEN points <= 750 THEN 5
    WHEN points <= 1050 THEN 6
    WHEN points <= 1400 THEN 7
    WHEN points <= 1800 THEN 8
    WHEN points <= 2250 THEN 9
    WHEN points <= 2750 THEN 10
    ELSE 11
END; 