-- Fix target values for list challenges
UPDATE challenges 
SET target_value = 3 
WHERE title = 'List 3 Items' AND target_value = 1;

UPDATE challenges 
SET target_value = 5 
WHERE title = 'List 5 Items' AND target_value = 1;

UPDATE challenges 
SET target_value = 10 
WHERE title = 'List 10 Items' AND target_value = 1;

UPDATE challenges 
SET target_value = 15 
WHERE title = 'List 15 Items' AND target_value = 1;

-- Fix target values for lesson completion challenges
UPDATE challenges 
SET target_value = 3 
WHERE title = 'Complete 3 Lessons' AND target_value = 1;

UPDATE challenges 
SET target_value = 6 
WHERE title = 'Complete 6 Lessons' AND target_value = 1;

-- Fix target values for buy challenges
UPDATE challenges 
SET target_value = 3 
WHERE title = 'Buy 3 Items' AND target_value = 1;

UPDATE challenges 
SET target_value = 5 
WHERE title = 'Buy 5 Items' AND target_value = 1;

UPDATE challenges 
SET target_value = 10 
WHERE title = 'Buy 10 Items' AND target_value = 1;

UPDATE challenges 
SET target_value = 15 
WHERE title = 'Buy 15 Items' AND target_value = 1; 