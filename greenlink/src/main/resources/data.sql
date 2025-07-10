-- Check if admin user doesn't exist before inserting
INSERT INTO users (email, password, first_name, last_name, enabled, active, role, created_at, points)
SELECT 'admin@greenlink.com', '$2a$10$dL4mR07/RW2VdgzUn9iQYOjp.vWkR1y4EzrabWR/YHhDhe0Y5EJ.O', 'Admin', 'User', true, true, 'ADMIN', CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@greenlink.com'
);

-- Insert default challenges if they don't exist
INSERT INTO challenges (title, description, points, type, status, start_date, end_date, updated_at, progress_percentage)
SELECT 'Reduce Water Usage', 'Take shorter showers and turn off the tap while brushing teeth to save water.', 50, 'DAILY', 'ACTIVE', CURRENT_DATE, CURRENT_DATE + 1, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'Reduce Water Usage'
);

INSERT INTO challenges (title, description, points, type, status, start_date, end_date, updated_at, progress_percentage)
SELECT 'Zero Waste Week', 'Avoid single-use plastics and properly recycle all materials for one week.', 200, 'WEEKLY', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '7 days', CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'Zero Waste Week'
);

INSERT INTO challenges (title, description, points, type, status, start_date, end_date, updated_at, progress_percentage)
SELECT 'Plant a Garden', 'Start and maintain a small herb or vegetable garden for sustainable food production.', 500, 'MONTHLY', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '30 days', CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'Plant a Garden'
);

INSERT INTO challenges (title, description, points, type, status, start_date, end_date, updated_at, progress_percentage)
SELECT 'Community Clean-up', 'Organize and participate in a local community clean-up event.', 1000, 'SPECIAL', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '90 days', CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'Community Clean-up'
);

-- Insert sample courses
INSERT INTO course (title, description, duration)
SELECT 'Introduction to Recycling', 'Learn the basics of recycling and how to properly sort different materials.', 60
WHERE NOT EXISTS (
    SELECT 1 FROM course WHERE title = 'Introduction to Recycling'
);

INSERT INTO course (title, description, duration)
SELECT 'Sustainable Living', 'Discover practical ways to live more sustainably and reduce your environmental impact.', 90
WHERE NOT EXISTS (
    SELECT 1 FROM course WHERE title = 'Sustainable Living'
);

INSERT INTO course (title, description, duration)
SELECT 'Energy Conservation', 'Learn effective strategies for reducing energy consumption at home and work.', 75
WHERE NOT EXISTS (
    SELECT 1 FROM course WHERE title = 'Energy Conservation'
);

-- Insert sample products
INSERT INTO products (name, description, price, category, image_url, eco_friendly, created_at, updated_at, stock, branch)
SELECT 'Bamboo Toothbrush', 'Eco-friendly bamboo toothbrush with biodegradable bristles', 9.99, 'BIO', 'Bamboo Toothbrush.png', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 100, 'VERDE'
WHERE NOT EXISTS (
    SELECT 1 FROM products WHERE name = 'Bamboo Toothbrush'
);

INSERT INTO products (name, description, price, category, image_url, eco_friendly, created_at, updated_at, stock, branch)
SELECT 'Organic Shampoo Bar', 'Zero-waste shampoo bar made with natural ingredients', 12.99, 'COSMETICS', 'Organic Shampoo Bar.png', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 50, 'VERDE'
WHERE NOT EXISTS (
    SELECT 1 FROM products WHERE name = 'Organic Shampoo Bar'
);

INSERT INTO products (name, description, price, category, image_url, eco_friendly, created_at, updated_at, stock, branch)
SELECT 'Recycled Paper Notebook', 'Notebook made from 100% recycled paper', 7.99, 'RECYCLED', 'Notebook.png', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 200, 'VERDE'
WHERE NOT EXISTS (
    SELECT 1 FROM products WHERE name = 'Recycled Paper Notebook'
); 