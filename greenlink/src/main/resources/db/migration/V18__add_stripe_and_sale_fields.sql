-- Add Stripe fields to users table
ALTER TABLE users 
ADD COLUMN stripe_account_id VARCHAR(255),
ADD COLUMN stripe_customer_id VARCHAR(255);

-- Add sale fields to products table
ALTER TABLE products 
ADD COLUMN sold BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN buyer_id BIGINT,
ADD COLUMN sold_at TIMESTAMP;

-- Add foreign key constraint for buyer
ALTER TABLE products
ADD CONSTRAINT fk_product_buyer
FOREIGN KEY (buyer_id) REFERENCES users (id);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_products_sold ON products(sold);
CREATE INDEX IF NOT EXISTS idx_products_seller_id ON products(seller_id);
CREATE INDEX IF NOT EXISTS idx_products_buyer_id ON products(buyer_id);
CREATE INDEX IF NOT EXISTS idx_users_stripe_account_id ON users(stripe_account_id);
CREATE INDEX IF NOT EXISTS idx_users_stripe_customer_id ON users(stripe_customer_id); 