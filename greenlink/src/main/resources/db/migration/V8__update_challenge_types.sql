-- Update challenge type constraint to allow new enum values
-- First, drop the existing constraint if it exists
DO $$
BEGIN
    -- Drop the constraint if it exists
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'challenges_type_check' 
        AND table_name = 'challenges'
    ) THEN
        ALTER TABLE challenges DROP CONSTRAINT challenges_type_check;
    END IF;
END $$;

-- Add new constraint with updated enum values
ALTER TABLE challenges ADD CONSTRAINT challenges_type_check 
CHECK (type IN ('DEFAULT', 'AMBASADOOR', 'MAESTER', 'SHELF_WHISPERER', 'CART_GOBLIN', 'ACTIVE', 'IN_PROGRESS', 'COMPLETED', 'EXPIRED'));

-- Also update status constraint if it exists
DO $$
BEGIN
    -- Drop the status constraint if it exists
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'challenges_status_check' 
        AND table_name = 'challenges'
    ) THEN
        ALTER TABLE challenges DROP CONSTRAINT challenges_status_check;
    END IF;
END $$;

-- Add new status constraint
ALTER TABLE challenges ADD CONSTRAINT challenges_status_check 
CHECK (status IN ('ACTIVE', 'IN_PROGRESS', 'COMPLETED', 'EXPIRED'));

-- Update any existing challenges with old enum values to new ones
UPDATE challenges SET type = 'DEFAULT' WHERE type = 'DAILY';
UPDATE challenges SET type = 'DEFAULT' WHERE type = 'WEEKLY';
UPDATE challenges SET type = 'DEFAULT' WHERE type = 'MONTHLY';
UPDATE challenges SET type = 'DEFAULT' WHERE type = 'SPECIAL'; 