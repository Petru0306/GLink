-- Add constraint for challenge types
ALTER TABLE challenges
ADD CONSTRAINT challenges_type_check CHECK (type IN ('DEFAULT_CHALLENGES', 'AMBASSADOR', 'MAESTER', 'SHELF_WHISPERER', 'CART_GOBLIN'));