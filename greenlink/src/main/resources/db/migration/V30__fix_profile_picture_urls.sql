-- Fix existing profile picture URLs to use the correct endpoint
UPDATE users 
SET profile_picture = REPLACE(profile_picture, '/uploads/profiles/', '/files/profiles/')
WHERE profile_picture LIKE '/uploads/profiles/%'; 