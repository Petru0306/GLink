-- Add OAuth2 fields to users table
ALTER TABLE users ADD COLUMN oauth2_provider VARCHAR(50);
ALTER TABLE users ADD COLUMN oauth2_provider_id VARCHAR(255);
ALTER TABLE users ADD COLUMN oauth2_name VARCHAR(255);
ALTER TABLE users ADD COLUMN oauth2_picture TEXT;

-- Create index for OAuth2 provider lookups
CREATE INDEX idx_users_oauth2_provider_id ON users(oauth2_provider, oauth2_provider_id); 