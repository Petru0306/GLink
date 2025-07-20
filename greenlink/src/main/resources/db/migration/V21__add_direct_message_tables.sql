-- Create direct message conversations table
CREATE TABLE direct_message_conversations (
    id BIGSERIAL PRIMARY KEY,
    user1_id BIGINT NOT NULL,
    user2_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_user1_read BOOLEAN NOT NULL DEFAULT FALSE,
    is_user2_read BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (user1_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (user2_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE (user1_id, user2_id)
);

-- Create direct messages table
CREATE TABLE direct_messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (conversation_id) REFERENCES direct_message_conversations(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_direct_messages_conversation ON direct_messages(conversation_id);
CREATE INDEX idx_direct_messages_sender ON direct_messages(sender_id);
CREATE INDEX idx_direct_messages_sent_at ON direct_messages(sent_at);
CREATE INDEX idx_direct_message_conversations_user1 ON direct_message_conversations(user1_id);
CREATE INDEX idx_direct_message_conversations_user2 ON direct_message_conversations(user2_id);
CREATE INDEX idx_direct_message_conversations_updated_at ON direct_message_conversations(updated_at);

-- Create trigger function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger for direct_message_conversations table
CREATE TRIGGER update_direct_message_conversations_updated_at 
    BEFORE UPDATE ON direct_message_conversations 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column(); 