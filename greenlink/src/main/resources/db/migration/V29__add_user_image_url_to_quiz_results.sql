-- Add user_image_url column to quiz_results table for storing final task photos
ALTER TABLE quiz_results ADD COLUMN user_image_url VARCHAR(500); 