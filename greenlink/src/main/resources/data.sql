-- Check if admin user doesn't exist before inserting
INSERT INTO users (email, password, first_name, last_name, enabled, active, role, created_at, points)
SELECT 'admin@greenlink.com', '$2a$10$mxddSR78Gl7K9hoXEZ2ahuTcJ/l3xToczgTiIFqOx951pK9CLX7Pe', 'Admin', 'User', true, true, 'ADMIN', CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@greenlink.com'
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

-- Insert products with images from resources folder
INSERT INTO products (name, description, price, category, image_url, eco_friendly, created_at, updated_at, stock, branch)
SELECT 'Bamboo Toothbrush', 'Eco-friendly bamboo toothbrush with biodegradable bristles', 9.99, 'BIO', '/images/products/Bamboo Toothbrush.png', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 100, 'VERDE'
WHERE NOT EXISTS (
    SELECT 1 FROM products WHERE name = 'Bamboo Toothbrush'
);

INSERT INTO products (name, description, price, category, image_url, eco_friendly, created_at, updated_at, stock, branch)
SELECT 'Organic Shampoo Bar', 'Zero-waste shampoo bar made with natural ingredients', 12.99, 'COSMETICS', '/images/products/Organic Shampoo Bar.png', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 50, 'VERDE'
WHERE NOT EXISTS (
    SELECT 1 FROM products WHERE name = 'Organic Shampoo Bar'
);

INSERT INTO products (name, description, price, category, image_url, eco_friendly, created_at, updated_at, stock, branch)
SELECT 'Recycled Paper Notebook', 'Notebook made from 100% recycled paper', 7.99, 'RECYCLED', '/images/products/Notebook.png', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 200, 'VERDE'
WHERE NOT EXISTS (
    SELECT 1 FROM products WHERE name = 'Recycled Paper Notebook'
);

-- Add additional products with existing images
INSERT INTO products (name, description, price, category, image_url, eco_friendly, created_at, updated_at, stock, branch)
SELECT 'Organic Cotton T-shirt', 'Eco-friendly t-shirt made from 100% organic cotton', 24.99, 'BIO', '/images/products/Organic cotton T-shirt.png', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 75, 'VERDE'
WHERE NOT EXISTS (
    SELECT 1 FROM products WHERE name = 'Organic Cotton T-shirt'
);

INSERT INTO products (name, description, price, category, image_url, eco_friendly, created_at, updated_at, stock, branch)
SELECT 'Wireless Earbuds', 'Refurbished wireless earbuds with excellent sound quality', 49.99, 'RECYCLED', '/images/products/Wireless earbuds.png', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 30, 'ELECTRO'
WHERE NOT EXISTS (
    SELECT 1 FROM products WHERE name = 'Wireless Earbuds'
);

INSERT INTO products (name, description, price, category, image_url, eco_friendly, created_at, updated_at, stock, branch)
SELECT 'Refurbished Laptop', 'Professionally refurbished laptop with 1-year warranty', 599.99, 'RECYCLED', '/images/products/Refurbished laptop.png', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 10, 'ELECTRO'
WHERE NOT EXISTS (
    SELECT 1 FROM products WHERE name = 'Refurbished Laptop'
);

INSERT INTO products (name, description, price, category, image_url, eco_friendly, created_at, updated_at, stock, branch)
SELECT 'Organic Honey', 'Pure organic honey from local beekeepers', 15.99, 'BIO', '/images/products/Organic honey.png', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 40, 'FOOD'
WHERE NOT EXISTS (
    SELECT 1 FROM products WHERE name = 'Organic Honey'
);

INSERT INTO products (name, description, price, category, image_url, eco_friendly, created_at, updated_at, stock, branch)
SELECT 'Artisan Whole-grain Bread', 'Fresh artisan bread made with organic whole grains', 7.50, 'BIO', '/images/products/Artisan whole-grain bread.png', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 25, 'FOOD'
WHERE NOT EXISTS (
    SELECT 1 FROM products WHERE name = 'Artisan Whole-grain Bread'
);

INSERT INTO products (name, description, price, category, image_url, eco_friendly, created_at, updated_at, stock, branch)
SELECT 'Dried Fruit Mix', 'Organic mixed dried fruits with no added sugar', 12.99, 'BIO', '/images/products/Dried fruit mix.png', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 50, 'FOOD'
WHERE NOT EXISTS (
    SELECT 1 FROM products WHERE name = 'Dried Fruit Mix'
); 

-- Set admin as the seller for all products
UPDATE products
SET seller_id = (SELECT id FROM users WHERE email = 'admin@greenlink.com')
WHERE seller_id IS NULL; 