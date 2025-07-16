-- -- Drop tables if they exist
-- DROP TABLE IF EXISTS materials_accepted;
-- DROP TABLE IF EXISTS user_challenges;
-- DROP TABLE IF EXISTS challenges;
-- DROP TABLE IF EXISTS quiz_results;
-- DROP TABLE IF EXISTS users;
-- DROP TABLE IF EXISTS course;
-- DROP TABLE IF EXISTS products;
-- DROP TABLE IF EXISTS recycling_point;

-- Create users table if not exists
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    bio TEXT,
    profile_picture VARCHAR(255),
    points INTEGER DEFAULT 0,
    created_at TIMESTAMP,
    last_login TIMESTAMP,
    enabled BOOLEAN DEFAULT true,
    active BOOLEAN DEFAULT true,
    role VARCHAR(50) DEFAULT 'USER'
);

-- Create challenges table
CREATE TABLE IF NOT EXISTS challenges (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    points INTEGER NOT NULL,
    badge VARCHAR(100),
    type VARCHAR(50),
    status VARCHAR(50),
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    completed_at TIMESTAMP,
    updated_at TIMESTAMP,
    progress_percentage INTEGER DEFAULT 0,
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create user_challenges table for challenge assignments
CREATE TABLE IF NOT EXISTS user_challenges (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    challenge_id BIGINT NOT NULL,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    completed_at TIMESTAMP,
    status VARCHAR(50),
    progress_percentage INTEGER DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (challenge_id) REFERENCES challenges(id)
);

-- Create course table
CREATE TABLE IF NOT EXISTS course (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    duration INTEGER NOT NULL
);

-- Create products table
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(50),
    image_url VARCHAR(255),
    eco_friendly BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    stock INTEGER NOT NULL,
    image_path VARCHAR(255)
); 