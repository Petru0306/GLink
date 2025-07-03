-- Add sample data
INSERT INTO recycling_point (name, address, latitude, longitude, description, schedule, contact_phone)
VALUES 
    ('Centru Reciclare Vest', 'Str. Verde nr. 123', 44.4268, 26.1025, 'Centru modern de reciclare cu facilități complete', 'Luni-Vineri: 09:00-17:00', '0722123456'),
    ('Centru Reciclare Est', 'Str. Reciclării nr. 45', 44.4308, 26.1125, 'Specializat în reciclarea plasticului și metalului', 'Luni-Sâmbătă: 08:00-18:00', '0722123457'),
    ('Punct Reciclare Central', 'Bulevardul Verde nr. 78', 44.4238, 26.0925, 'Punct de colectare pentru toate tipurile de materiale', 'Non-stop', '0722123458');

-- Insert materials for each recycling point
INSERT INTO materials_accepted (recycling_point_id, material)
SELECT id, 'plastic' FROM recycling_point WHERE name = 'Centru Reciclare Vest'
UNION ALL
SELECT id, 'hartie' FROM recycling_point WHERE name = 'Centru Reciclare Vest'
UNION ALL
SELECT id, 'sticla' FROM recycling_point WHERE name = 'Centru Reciclare Vest'
UNION ALL
SELECT id, 'plastic' FROM recycling_point WHERE name = 'Centru Reciclare Est'
UNION ALL
SELECT id, 'metal' FROM recycling_point WHERE name = 'Centru Reciclare Est'
UNION ALL
SELECT id, 'sticla' FROM recycling_point WHERE name = 'Centru Reciclare Est'
UNION ALL
SELECT id, 'plastic' FROM recycling_point WHERE name = 'Punct Reciclare Central'
UNION ALL
SELECT id, 'hartie' FROM recycling_point WHERE name = 'Punct Reciclare Central'
UNION ALL
SELECT id, 'sticla' FROM recycling_point WHERE name = 'Punct Reciclare Central'
UNION ALL
SELECT id, 'metal' FROM recycling_point WHERE name = 'Punct Reciclare Central'; 