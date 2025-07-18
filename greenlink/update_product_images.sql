-- SQL script to pair products with existing images in resources
CREATE OR REPLACE FUNCTION update_product_images() RETURNS void AS }
BEGIN
    -- Update existing products to use images from resources
    UPDATE products SET image_url = '/images/products/Bamboo Toothbrush.png' WHERE name = 'Bamboo Toothbrush';
    UPDATE products SET image_url = '/images/products/Organic Shampoo Bar.png' WHERE name = 'Organic Shampoo Bar';
    UPDATE products SET image_url = '/images/products/Notebook.png' WHERE name = 'Recycled Paper Notebook';
    
    -- Add missing products with their images
    INSERT INTO products (name, description, price, category, image_url, eco_friendly, created_at, updated_at, stock, branch)
    SELECT 'Organic Cotton T-shirt', 'Eco-friendly t-shirt made from 100% organic cotton', 24.99, 'BIO', '/images/products/Organic cotton T-shirt.png', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 75, 'VERDE'
    WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Organic Cotton T-shirt');
    
    INSERT INTO products (name, description, price, category, image_url, eco_friendly, created_at, updated_at, stock, branch)
    SELECT 'Wireless Earbuds', 'Refurbished wireless earbuds with excellent sound quality', 49.99, 'RECYCLED', '/images/products/Wireless earbuds.png', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 30, 'ELECTRO'
    WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Wireless Earbuds');
    
    INSERT INTO products (name, description, price, category, image_url, eco_friendly, created_at, updated_at, stock, branch)
    SELECT 'Refurbished Laptop', 'Professionally refurbished laptop with 1-year warranty', 599.99, 'RECYCLED', '/images/products/Refurbished laptop.png', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 10, 'ELECTRO'
    WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Refurbished Laptop');
    
    INSERT INTO products (name, description, price, category, image_url, eco_friendly, created_at, updated_at, stock, branch)
    SELECT 'Organic Honey', 'Pure organic honey from local beekeepers', 15.99, 'BIO', '/images/products/Organic honey.png', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 40, 'FOOD'
    WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Organic Honey');
    
    INSERT INTO products (name, description, price, category, image_url, eco_friendly, created_at, updated_at, stock, branch)
    SELECT 'Artisan Whole-grain Bread', 'Fresh artisan bread made with organic whole grains', 7.50, 'BIO', '/images/products/Artisan whole-grain bread.png', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 25, 'FOOD'
    WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Artisan Whole-grain Bread');
    
    INSERT INTO products (name, description, price, category, image_url, eco_friendly, created_at, updated_at, stock, branch)
    SELECT 'Dried Fruit Mix', 'Organic mixed dried fruits with no added sugar', 12.99, 'BIO', '/images/products/Dried fruit mix.png', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 50, 'FOOD'
    WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Dried Fruit Mix');
END;
} LANGUAGE plpgsql;

-- Execute the function
SELECT update_product_images();

-- Drop the function after use
DROP FUNCTION update_product_images();

