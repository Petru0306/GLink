-- Delete user challenges for all challenges to be removed
DELETE FROM user_challenges 
WHERE challenge_id IN (
    SELECT id 
    FROM challenges 
    WHERE title IN (
        'Create Your Eco Avatar',
        'Complete Your First Lesson',
        'Upload Your First Photo',
        'Captain Compostface',
        'The Leaf Whisperer',
        'Shutter the Litter',
        'The Sorting Sprout',
        'Take the Eco Personality Quiz'
    )
    AND type = 'DEFAULT_CHALLENGES'
);

-- Delete the challenges themselves
DELETE FROM challenges 
WHERE title IN (
    'Create Your Eco Avatar',
    'Complete Your First Lesson',
    'Upload Your First Photo',
    'Captain Compostface',
    'The Leaf Whisperer',
    'Shutter the Litter',
    'The Sorting Sprout',
    'Take the Eco Personality Quiz'
)
AND type = 'DEFAULT_CHALLENGES';
