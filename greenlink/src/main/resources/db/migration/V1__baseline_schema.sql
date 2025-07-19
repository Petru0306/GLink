-- Baseline schema migration - replaces schema.sql
-- This creates the initial database structure

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    bio TEXT,
    profile_picture VARCHAR(255),
    points INTEGER DEFAULT 0,
    level INTEGER DEFAULT 1,
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
    target_value INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
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
    current_value INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    seen BOOLEAN NOT NULL DEFAULT FALSE,
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
    image_path VARCHAR(255),
    branch VARCHAR(50) DEFAULT 'VERDE',
    seller_id BIGINT,
    sold BOOLEAN NOT NULL DEFAULT FALSE,
    buyer_id BIGINT,
    sold_at TIMESTAMP,
    FOREIGN KEY (seller_id) REFERENCES users(id),
    FOREIGN KEY (buyer_id) REFERENCES users(id)
);

-- Create friends table
CREATE TABLE IF NOT EXISTS friends (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (friend_id) REFERENCES users(id),
    UNIQUE (user_id, friend_id)
);

-- Create point_events table
CREATE TABLE IF NOT EXISTS point_events (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    points INTEGER NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    related_entity_id BIGINT,
    related_entity_type VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    old_level INTEGER,
    new_level INTEGER,
    leveled_up BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create conversations table
CREATE TABLE IF NOT EXISTS conversations (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    is_seller_read BOOLEAN NOT NULL DEFAULT FALSE,
    is_buyer_read BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (seller_id) REFERENCES users(id),
    FOREIGN KEY (buyer_id) REFERENCES users(id)
);

-- Create messages table
CREATE TABLE IF NOT EXISTS messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    sent_at TIMESTAMP NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    offer_amount DOUBLE PRECISION,
    offer_status VARCHAR(20),
    FOREIGN KEY (conversation_id) REFERENCES conversations(id),
    FOREIGN KEY (sender_id) REFERENCES users(id)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_point_events_user_id ON point_events(user_id);
CREATE INDEX IF NOT EXISTS idx_point_events_event_type ON point_events(event_type);
CREATE INDEX IF NOT EXISTS idx_point_events_created_at ON point_events(created_at);
CREATE INDEX IF NOT EXISTS idx_point_events_leveled_up ON point_events(leveled_up);

CREATE INDEX IF NOT EXISTS idx_conversation_product ON conversations(product_id);
CREATE INDEX IF NOT EXISTS idx_conversation_seller ON conversations(seller_id);
CREATE INDEX IF NOT EXISTS idx_conversation_buyer ON conversations(buyer_id);
CREATE INDEX IF NOT EXISTS idx_message_conversation ON messages(conversation_id);
CREATE INDEX IF NOT EXISTS idx_message_sender ON messages(sender_id);

CREATE INDEX IF NOT EXISTS idx_products_sold ON products(sold);
CREATE INDEX IF NOT EXISTS idx_products_seller_id ON products(seller_id);
CREATE INDEX IF NOT EXISTS idx_products_buyer_id ON products(buyer_id); 