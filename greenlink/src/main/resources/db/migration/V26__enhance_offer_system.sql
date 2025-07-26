-- Add version column for optimistic locking (if not exists)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'messages' AND column_name = 'version') THEN
        ALTER TABLE messages ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
    END IF;
END $$;

-- Add offer expiration tracking (if not exists)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'messages' AND column_name = 'offer_expires_at') THEN
        ALTER TABLE messages ADD COLUMN offer_expires_at TIMESTAMP;
    END IF;
END $$;

-- Add offer chain references for counter-offers (if not exists)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'messages' AND column_name = 'counter_offer_message_id') THEN
        ALTER TABLE messages ADD COLUMN counter_offer_message_id BIGINT;
    END IF;
END $$;

DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'messages' AND column_name = 'original_offer_message_id') THEN
        ALTER TABLE messages ADD COLUMN original_offer_message_id BIGINT;
    END IF;
END $$;

-- Add indexes for better performance (if not exist)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_messages_offer_status') THEN
        CREATE INDEX idx_messages_offer_status ON messages(offer_status);
    END IF;
END $$;

DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_messages_offer_expires') THEN
        CREATE INDEX idx_messages_offer_expires ON messages(offer_expires_at);
    END IF;
END $$;

DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_messages_original_offer') THEN
        CREATE INDEX idx_messages_original_offer ON messages(original_offer_message_id);
    END IF;
END $$;

DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_messages_counter_offer') THEN
        CREATE INDEX idx_messages_counter_offer ON messages(counter_offer_message_id);
    END IF;
END $$;

-- Add foreign key constraints for offer chain references (if not exist)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'fk_messages_counter_offer') THEN
        ALTER TABLE messages 
        ADD CONSTRAINT fk_messages_counter_offer 
        FOREIGN KEY (counter_offer_message_id) REFERENCES messages(id);
    END IF;
END $$;

DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'fk_messages_original_offer') THEN
        ALTER TABLE messages 
        ADD CONSTRAINT fk_messages_original_offer 
        FOREIGN KEY (original_offer_message_id) REFERENCES messages(id);
    END IF;
END $$;

-- Update existing offers to have expiration dates (7 days from sent_at)
UPDATE messages 
SET offer_expires_at = sent_at + INTERVAL '7 days'
WHERE offer_amount IS NOT NULL AND offer_expires_at IS NULL;

-- Update existing offers to use ACTION_REQUIRED status instead of PENDING for better UX
UPDATE messages 
SET offer_status = 'ACTION_REQUIRED'
WHERE offer_status = 'PENDING' AND offer_amount IS NOT NULL; 