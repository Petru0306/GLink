-- Add delivery conversation columns to products table
ALTER TABLE products ADD COLUMN IF NOT EXISTS conversation_id BIGINT;

-- Add delivery status columns to conversations table
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'OPEN';
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS delivery_status VARCHAR(50) DEFAULT 'PENDING';
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS delivery_completed_at TIMESTAMP;

-- Add foreign key constraint for conversation_id in products table
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_products_conversation_id' 
        AND table_name = 'products'
    ) THEN
        ALTER TABLE products ADD CONSTRAINT fk_products_conversation_id 
        FOREIGN KEY (conversation_id) REFERENCES conversations(id);
    END IF;
END $$; 