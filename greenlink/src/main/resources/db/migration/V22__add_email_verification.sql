-- Add email verification fields to users table (allowing NULL initially)
ALTER TABLE users ADD COLUMN email_verified BOOLEAN;
ALTER TABLE users ADD COLUMN verification_token VARCHAR(255);
ALTER TABLE users ADD COLUMN verification_token_expiry TIMESTAMP;

-- Update existing users as verified
UPDATE users SET email_verified = TRUE;

-- Now set the NOT NULL constraint
ALTER TABLE users ALTER COLUMN email_verified SET NOT NULL;
ALTER TABLE users ALTER COLUMN email_verified SET DEFAULT FALSE;

-- Add indexes for efficient lookup
CREATE INDEX idx_users_verification_token ON users(verification_token);
