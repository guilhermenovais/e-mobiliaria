ALTER TABLE payment_proofs
    ADD COLUMN display_name VARCHAR(255);
UPDATE payment_proofs
SET display_name = original_filename
WHERE display_name IS NULL;
ALTER TABLE payment_proofs
    ALTER COLUMN display_name SET NOT NULL;
