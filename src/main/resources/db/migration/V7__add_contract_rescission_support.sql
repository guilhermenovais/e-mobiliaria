ALTER TABLE contracts
    ADD COLUMN IF NOT EXISTS rescinded_at DATE;
