-- Replace the hash with the one generated from /auth-test/generate-password?password=yourNewPassword
UPDATE users 
SET password = '$2a$10$newPasswordHashHere'
WHERE email = 'admin@greenlink.com'; 