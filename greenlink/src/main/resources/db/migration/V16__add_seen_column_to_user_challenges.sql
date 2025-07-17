-- Add seen column to user_challenges table
ALTER TABLE user_challenges ADD COLUMN seen BOOLEAN NOT NULL DEFAULT FALSE;

-- Set existing challenges as seen
UPDATE user_challenges SET seen = TRUE WHERE status = 'COMPLETED';
