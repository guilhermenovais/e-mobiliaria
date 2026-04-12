CREATE TABLE juridical_person_representatives (
    juridical_person_id BIGINT NOT NULL REFERENCES juridical_persons(id) ON DELETE CASCADE,
    physical_person_id  BIGINT NOT NULL REFERENCES physical_persons(id),
    PRIMARY KEY (juridical_person_id, physical_person_id)
);

INSERT INTO juridical_person_representatives (juridical_person_id, physical_person_id)
SELECT id, representative_id FROM juridical_persons;

ALTER TABLE juridical_persons DROP COLUMN representative_id;
