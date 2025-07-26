-- Fix the offer_status constraint to include the new ACTION_REQUIRED status
-- First, drop the existing constraint if it exists
DO $$ 
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.table_constraints 
               WHERE table_name = 'messages' 
               AND constraint_name = 'messages_offer_status_check') THEN
        ALTER TABLE messages DROP CONSTRAINT messages_offer_status_check;
    END IF;
END $$;

-- Add the new constraint with all valid offer statuses
ALTER TABLE messages 
ADD CONSTRAINT messages_offer_status_check 
CHECK (offer_status IS NULL OR offer_status IN ('PENDING', 'ACTION_REQUIRED', 'ACCEPTED', 'REJECTED', 'COUNTERED', 'EXPIRED', 'CANCELLED')); 