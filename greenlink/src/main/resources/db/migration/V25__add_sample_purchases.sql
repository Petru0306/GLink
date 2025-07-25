-- Add sample purchases for testing buy history functionality
-- This migration creates some test purchases for the admin user

-- First, let's create a regular user to be the buyer
INSERT INTO users (email, password, first_name, last_name, enabled, active, role, created_at, points)
SELECT 'buyer@test.com', '$2a$10$mxddSR78Gl7K9hoXEZ2ahuTcJ/l3xToczgTiIFqOx951pK9CLX7Pe', 'Test', 'Buyer', true, true, 'USER', CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'buyer@test.com'
);

-- Mark some products as sold to the test buyer
UPDATE products 
SET sold = true, 
    buyer_id = (SELECT id FROM users WHERE email = 'buyer@test.com'),
    sold_at = CURRENT_TIMESTAMP - INTERVAL '5 days'
WHERE name = 'Bamboo Toothbrush' 
AND sold = false;

UPDATE products 
SET sold = true, 
    buyer_id = (SELECT id FROM users WHERE email = 'buyer@test.com'),
    sold_at = CURRENT_TIMESTAMP - INTERVAL '3 days'
WHERE name = 'Organic Shampoo Bar' 
AND sold = false;

UPDATE products 
SET sold = true, 
    buyer_id = (SELECT id FROM users WHERE email = 'buyer@test.com'),
    sold_at = CURRENT_TIMESTAMP - INTERVAL '1 day'
WHERE name = 'Recycled Paper Notebook' 
AND sold = false;

-- Also mark some products as sold to the admin user for testing
UPDATE products 
SET sold = true, 
    buyer_id = (SELECT id FROM users WHERE email = 'admin@greenlink.com'),
    sold_at = CURRENT_TIMESTAMP - INTERVAL '7 days'
WHERE name = 'Organic Cotton T-shirt' 
AND sold = false;

UPDATE products 
SET sold = true, 
    buyer_id = (SELECT id FROM users WHERE email = 'admin@greenlink.com'),
    sold_at = CURRENT_TIMESTAMP - INTERVAL '2 days'
WHERE name = 'Wireless Earbuds' 
AND sold = false; 