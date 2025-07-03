CREATE TABLE recycling_point (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    description TEXT,
    schedule VARCHAR(255),
    contact_phone VARCHAR(20)
);

CREATE TABLE materials_accepted (
    recycling_point_id BIGINT NOT NULL,
    material VARCHAR(255) NOT NULL,
    FOREIGN KEY (recycling_point_id) REFERENCES recycling_point(id)
); 