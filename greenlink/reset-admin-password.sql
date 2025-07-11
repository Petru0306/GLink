-- Replace the hash with the one generated from /auth-test/generate-password?password=yourNewPassword
UPDATE users 
SET password = '$2a$10$mxddSR78Gl7K9hoXEZ2ahuTcJ/l3xToczgTiIFqOx951pK9CLX7Pe'
WHERE email = 'admin@greenlink.com'; 