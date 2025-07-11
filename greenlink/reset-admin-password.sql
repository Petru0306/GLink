-- Replace the hash with the one generated from /auth-test/generate-password?password=yourNewPassword
UPDATE users 
SET password = '$2a$12$r7vlvYOz2bkxGZC4Env95ugHx.NQWMLyMoE8lfU9QsvK8zmPd17Dm'
WHERE email = 'admin@greenlink.com'; 