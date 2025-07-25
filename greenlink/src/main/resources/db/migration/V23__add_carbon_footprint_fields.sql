-- Add carbon footprint fields to users table
ALTER TABLE users ADD COLUMN carbon_footprint_tonnes DOUBLE;
ALTER TABLE users ADD COLUMN carbon_footprint_date TIMESTAMP;
ALTER TABLE users ADD COLUMN carbon_footprint_category VARCHAR(50); 