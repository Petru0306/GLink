-- Update existing quiz records with proper titles and descriptions
-- This migration fixes the placeholder quiz data that was created with generic titles

-- Update quiz with ID 1
UPDATE quizzes 
SET title = 'Ce este sustenabilitatea?',
    description = 'Învață principiile fundamentale ale sustenabilității și cum să aplici aceste concepte în viața de zi cu zi.'
WHERE id = 1 AND title = 'Course 1';

-- Update quiz with ID 2
UPDATE quizzes 
SET title = 'Cum să reciclezi corect',
    description = 'Descoperă regulile de bază pentru reciclare și cum să organizezi deșeurile eficient.'
WHERE id = 2 AND title = 'Course 2';

-- Update quiz with ID 3
UPDATE quizzes 
SET title = 'Reduce deșeurile cu o singură schimbare',
    description = 'Învață schimbări mici care au un impact mare în reducerea deșeurilor din casa ta.'
WHERE id = 3 AND title = 'Course 3';

-- Update quiz with ID 4
UPDATE quizzes 
SET title = 'Începe un mini-compost acasă',
    description = 'Transformă resturile în viață nouă'
WHERE id = 4 AND title = 'Course 4';

-- Update quiz with ID 5
UPDATE quizzes 
SET title = 'Plantează ceva (chiar și într-un ghiveci!)',
    description = 'Conectează-te cu natura prin plantat'
WHERE id = 5 AND title = 'Course 5';

-- Update quiz with ID 6
UPDATE quizzes 
SET title = 'Adună gunoiul din cartierul tău',
    description = 'Fă diferența în comunitatea ta'
WHERE id = 6 AND title = 'Course 6'; 