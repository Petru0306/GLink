-- Add sample conversations (only run if there are users and products)
INSERT INTO conversations (product_id, seller_id, buyer_id, created_at, updated_at, is_seller_read, is_buyer_read)
SELECT p.id, p.seller_id, u.id, NOW(), NOW(), false, true
FROM products p
JOIN users u ON u.id != p.seller_id
WHERE p.id = (SELECT MIN(id) FROM products)
  AND EXISTS (SELECT 1 FROM users WHERE id = p.seller_id)
  AND EXISTS (SELECT 1 FROM users WHERE id != p.seller_id)
LIMIT 1;

-- Add sample messages
INSERT INTO messages (conversation_id, sender_id, content, sent_at, is_read)
SELECT c.id, c.buyer_id, 'Hello, I am interested in this product!', NOW() - INTERVAL 2 HOUR, true
FROM conversations c
LIMIT 1;

INSERT INTO messages (conversation_id, sender_id, content, sent_at, is_read)
SELECT c.id, c.seller_id, 'Hi there! Thank you for your interest. Do you have any questions?', NOW() - INTERVAL 1 HOUR, false
FROM conversations c
LIMIT 1;

-- Add sample offer
INSERT INTO messages (conversation_id, sender_id, content, sent_at, is_read, offer_amount, offer_status)
SELECT c.id, c.buyer_id, 'Offered 90.00 RON for this product', NOW(), false, 90.00, 'PENDING'
FROM conversations c
LIMIT 1; 