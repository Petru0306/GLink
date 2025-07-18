-- Remove First Item Listed challenge from DEFAULT_CHALLENGES
DELETE FROM user_challenges
WHERE challenge_id IN (
    SELECT id FROM challenges
    WHERE title = 'First Item Listed'
    AND type = 'DEFAULT_CHALLENGES'
);

-- After removing references, remove the challenge itself
DELETE FROM challenges
WHERE title = 'First Item Listed'
AND type = 'DEFAULT_CHALLENGES';
