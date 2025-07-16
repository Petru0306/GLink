-- Remove specific DEFAULT_CHALLENGES as requested
-- Remove Captain Compostface
DELETE FROM challenges 
WHERE title = 'Captain Compostface' 
AND type = 'DEFAULT_CHALLENGES';

-- Remove The Leaf Whisperer
DELETE FROM challenges 
WHERE title = 'The Leaf Whisperer' 
AND type = 'DEFAULT_CHALLENGES';

-- Remove Shutter the Litter
DELETE FROM challenges 
WHERE title = 'Shutter the Litter' 
AND type = 'DEFAULT_CHALLENGES';

-- Remove The Sorting Sprout
DELETE FROM challenges 
WHERE title = 'The Sorting Sprout' 
AND type = 'DEFAULT_CHALLENGES';

-- Also remove any related user_challenges for these deleted challenges
DELETE FROM user_challenges 
WHERE challenge_id IN (
    SELECT id FROM challenges 
    WHERE title IN ('Captain Compostface', 'The Leaf Whisperer', 'Shutter the Litter', 'The Sorting Sprout')
    AND type = 'DEFAULT_CHALLENGES'
); 