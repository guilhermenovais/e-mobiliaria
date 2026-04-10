ALTER TABLE contracts ADD COLUMN purpose VARCHAR(100) NOT NULL DEFAULT 'Comercial';
ALTER TABLE properties DROP COLUMN purpose;
