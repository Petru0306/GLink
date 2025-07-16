-- Migration to remove "The Eco Cult Leader" challenge from the database

-- First, remove any user progress for this challenge
DELETE FROM user_challenges 
WHERE challenge_id IN (
    SELECT id FROM challenges 
    WHERE title = 'Fifteen Friends' 
    AND badge = 'The Eco Cult Leader'
    AND type = 'AMBASSADOR'
);

-- Then, remove the challenge itself
DELETE FROM challenges 
WHERE title = 'Fifteen Friends' 
AND badge = 'The Eco Cult Leader'
AND type = 'AMBASSADOR';
