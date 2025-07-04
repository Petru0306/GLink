ALTER TABLE products
ADD COLUMN seller_id BIGINT;

ALTER TABLE products
ADD CONSTRAINT fk_product_seller
FOREIGN KEY (seller_id) REFERENCES users (id); 