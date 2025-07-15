-- Drop existing tables
DROP TABLE IF EXISTS user_challenges;
DROP TABLE IF EXISTS challenges;

-- Create new challenges table
CREATE TABLE challenges (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    points INTEGER NOT NULL,
    category VARCHAR(50) NOT NULL,
    badge_name VARCHAR(255) NOT NULL,
    emoji VARCHAR(10) NOT NULL,
    target_value INTEGER NOT NULL,
    progress_event VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create new user_challenges table
CREATE TABLE user_challenges (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    challenge_id BIGINT NOT NULL,
    current_value INTEGER DEFAULT 0,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    status VARCHAR(20) DEFAULT 'NOT_STARTED',
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (challenge_id) REFERENCES challenges(id),
    UNIQUE (user_id, challenge_id)
);

-- Create indexes for better performance
CREATE INDEX idx_challenges_category ON challenges(category);
CREATE INDEX idx_challenges_progress_event ON challenges(progress_event);
CREATE INDEX idx_user_challenges_user_id ON user_challenges(user_id);
CREATE INDEX idx_user_challenges_status ON user_challenges(status);
CREATE INDEX idx_user_challenges_completed_at ON user_challenges(completed_at); 