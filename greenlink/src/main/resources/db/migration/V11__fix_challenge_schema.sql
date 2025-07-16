-- Fix challenges table schema
-- Remove user_id from challenges table (challenges are global templates)
ALTER TABLE challenges DROP COLUMN IF EXISTS user_id;
ALTER TABLE challenges DROP COLUMN IF EXISTS status;
ALTER TABLE challenges DROP COLUMN IF EXISTS start_date;
ALTER TABLE challenges DROP COLUMN IF EXISTS end_date;
ALTER TABLE challenges DROP COLUMN IF EXISTS completed_at;
ALTER TABLE challenges DROP COLUMN IF EXISTS updated_at;
ALTER TABLE challenges DROP COLUMN IF EXISTS progress_percentage;

-- Add missing columns to challenges table
ALTER TABLE challenges ADD COLUMN IF NOT EXISTS target_value INTEGER DEFAULT 1;
ALTER TABLE challenges ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Add missing columns to user_challenges table
ALTER TABLE user_challenges ADD COLUMN IF NOT EXISTS current_value INTEGER DEFAULT 0;
ALTER TABLE user_challenges ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP; 