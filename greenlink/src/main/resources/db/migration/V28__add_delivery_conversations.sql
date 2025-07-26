-- Add conversation_id to products table to link products to delivery conversations
ALTER TABLE products 
ADD COLUMN IF NOT EXISTS conversation_id BIGINT;

-- Add foreign key constraint for conversation (only if it doesn't exist)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'fk_product_conversation') THEN
        ALTER TABLE products
        ADD CONSTRAINT fk_product_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversations (id);
    END IF;
END $$;

-- Add status field to conversations table
ALTER TABLE conversations 
ADD COLUMN IF NOT EXISTS status VARCHAR(50) NOT NULL DEFAULT 'OPEN';

-- Add delivery_status field to conversations table
ALTER TABLE conversations 
ADD COLUMN IF NOT EXISTS delivery_status VARCHAR(50) NOT NULL DEFAULT 'PENDING';

-- Add delivery_completed_at timestamp
ALTER TABLE conversations 
ADD COLUMN IF NOT EXISTS delivery_completed_at TIMESTAMP;

-- Add indexes for better performance (only if they don't exist)
CREATE INDEX IF NOT EXISTS idx_products_conversation_id ON products(conversation_id);
CREATE INDEX IF NOT EXISTS idx_conversations_status ON conversations(status);
CREATE INDEX IF NOT EXISTS idx_conversations_delivery_status ON conversations(delivery_status);

-- Add constraint to ensure valid status values (only if it doesn't exist)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'chk_conversation_status') THEN
        ALTER TABLE conversations 
        ADD CONSTRAINT chk_conversation_status 
        CHECK (status IN ('OPEN', 'CLOSED', 'COMPLETED'));
    END IF;
END $$;

-- Add constraint to ensure valid delivery status values (only if it doesn't exist)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'chk_delivery_status') THEN
        ALTER TABLE conversations 
        ADD CONSTRAINT chk_delivery_status 
        CHECK (delivery_status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED'));
    END IF;
END $$; 