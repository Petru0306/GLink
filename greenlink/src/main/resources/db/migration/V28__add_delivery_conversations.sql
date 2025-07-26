-- Add conversation_id to products table to link products to delivery conversations
ALTER TABLE products 
ADD COLUMN conversation_id BIGINT;

-- Add foreign key constraint for conversation
ALTER TABLE products
ADD CONSTRAINT fk_product_conversation
FOREIGN KEY (conversation_id) REFERENCES conversations (id);

-- Add status field to conversations table
ALTER TABLE conversations 
ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'OPEN';

-- Add delivery_status field to conversations table
ALTER TABLE conversations 
ADD COLUMN delivery_status VARCHAR(50) NOT NULL DEFAULT 'PENDING';

-- Add delivery_completed_at timestamp
ALTER TABLE conversations 
ADD COLUMN delivery_completed_at TIMESTAMP;

-- Add indexes for better performance
CREATE INDEX IF NOT EXISTS idx_products_conversation_id ON products(conversation_id);
CREATE INDEX IF NOT EXISTS idx_conversations_status ON conversations(status);
CREATE INDEX IF NOT EXISTS idx_conversations_delivery_status ON conversations(delivery_status);

-- Add constraint to ensure valid status values
ALTER TABLE conversations 
ADD CONSTRAINT chk_conversation_status 
CHECK (status IN ('OPEN', 'CLOSED', 'COMPLETED'));

-- Add constraint to ensure valid delivery status values
ALTER TABLE conversations 
ADD CONSTRAINT chk_delivery_status 
CHECK (delivery_status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED')); 