-- Add missing listing challenges that the code expects
INSERT INTO challenges (title, description, type, points, badge, target_value, created_at)
SELECT 'List 10 Items', 'List 10 items in the marketplace', 'SHELF_WHISPERER', 200, 'List Master Badge', 10, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'List 10 Items'
);

INSERT INTO challenges (title, description, type, points, badge, target_value, created_at)
SELECT 'List 15 Items', 'List 15 items in the marketplace', 'SHELF_WHISPERER', 300, 'List Expert Badge', 15, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'List 15 Items'
);

INSERT INTO challenges (title, description, type, points, badge, target_value, created_at)
SELECT 'List 3 Items', 'List 3 items in the marketplace', 'SHELF_WHISPERER', 100, 'List Beginner Badge', 3, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'List 3 Items'
);

INSERT INTO challenges (title, description, type, points, badge, target_value, created_at)
SELECT 'List 5 Items', 'List 5 items in the marketplace', 'SHELF_WHISPERER', 150, 'List Intermediate Badge', 5, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'List 5 Items'
); 