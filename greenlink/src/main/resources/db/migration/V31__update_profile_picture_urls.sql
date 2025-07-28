-- Update existing profile picture URLs to use the /files/profiles/ endpoint
UPDATE users 
SET profile_picture = REPLACE(profile_picture, '/uploads/profiles/', '/files/profiles/')
WHERE profile_picture LIKE '/uploads/profiles/%'; 