-- Add sample challenges
INSERT INTO challenges (title, description, type, points, badge, target_value, created_at)
SELECT 'First Steps', 'Complete your first lesson in the education section', 'DEFAULT_CHALLENGES', 50, 'Student Badge', 1, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'First Steps'
);

INSERT INTO challenges (title, description, type, points, badge, target_value, created_at)
SELECT 'Knowledge Seeker', 'Complete 3 lessons in the education section', 'DEFAULT_CHALLENGES', 100, 'Scholar Badge', 3, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'Knowledge Seeker'
);

INSERT INTO challenges (title, description, type, points, badge, target_value, created_at)
SELECT 'Marketplace Explorer', 'List your first item in the marketplace', 'DEFAULT_CHALLENGES', 75, 'Trader Badge', 1, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'Marketplace Explorer'
);

INSERT INTO challenges (title, description, type, points, badge, target_value, created_at)
SELECT 'Social Butterfly', 'Send your first message to another user', 'DEFAULT_CHALLENGES', 25, 'Communicator Badge', 1, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'Social Butterfly'
);

INSERT INTO challenges (title, description, type, points, badge, target_value, created_at)
SELECT 'Carbon Conscious', 'Use the carbon calculator for the first time', 'DEFAULT_CHALLENGES', 50, 'Calculator Badge', 1, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'Carbon Conscious'
);

INSERT INTO challenges (title, description, type, points, badge, target_value, created_at)
SELECT 'Recycling Pioneer', 'Explore the recycling map for the first time', 'DEFAULT_CHALLENGES', 50, 'Explorer Badge', 1, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'Recycling Pioneer'
);

INSERT INTO challenges (title, description, type, points, badge, target_value, created_at)
SELECT 'Profile Perfectionist', 'Upload a profile photo', 'DEFAULT_CHALLENGES', 30, 'Photographer Badge', 1, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'Profile Perfectionist'
);

INSERT INTO challenges (title, description, type, points, badge, target_value, created_at)
SELECT 'Deal Maker', 'Make your first offer in the marketplace', 'DEFAULT_CHALLENGES', 40, 'Negotiator Badge', 1, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'Deal Maker'
);

-- Ambassador challenges
INSERT INTO challenges (title, description, type, points, badge, target_value, created_at)
SELECT 'Community Leader', 'Complete 10 lessons and help 5 other users', 'AMBASSADOR', 200, 'Ambassador Badge', 10, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'Community Leader'
);

INSERT INTO challenges (title, description, type, points, badge, target_value, created_at)
SELECT 'Green Advocate', 'List 10 eco-friendly products in the marketplace', 'AMBASSADOR', 300, 'Green Advocate Badge', 10, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'Green Advocate'
);

-- Maester challenges
INSERT INTO challenges (title, description, type, points, badge, target_value, created_at)
SELECT 'Education Master', 'Complete all available lessons', 'MAESTER', 500, 'Maester Badge', 5, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'Education Master'
);

INSERT INTO challenges (title, description, type, points, badge, target_value, created_at)
SELECT 'Carbon Expert', 'Use the carbon calculator 10 times', 'MAESTER', 400, 'Carbon Expert Badge', 10, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'Carbon Expert'
);

-- Shelf Whisperer challenges
INSERT INTO challenges (title, description, type, points, badge, target_value, created_at)
SELECT 'Product Curator', 'List 20 products in the marketplace', 'SHELF_WHISPERER', 400, 'Curator Badge', 20, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'Product Curator'
);

INSERT INTO challenges (title, description, type, points, badge, target_value, created_at)
SELECT 'Marketplace Veteran', 'Make 15 successful transactions', 'SHELF_WHISPERER', 600, 'Veteran Badge', 15, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'Marketplace Veteran'
);

-- Cart Goblin challenges
INSERT INTO challenges (title, description, type, points, badge, target_value, created_at)
SELECT 'Bargain Hunter', 'Make 10 offers in the marketplace', 'CART_GOBLIN', 300, 'Hunter Badge', 10, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'Bargain Hunter'
);

INSERT INTO challenges (title, description, type, points, badge, target_value, created_at)
SELECT 'Deal Master', 'Successfully negotiate 5 deals', 'CART_GOBLIN', 500, 'Deal Master Badge', 5, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'Deal Master'
); 