BEGIN ATOMIC
    DECLARE cpf_constraint_name VARCHAR;
    DECLARE cnpj_constraint_name VARCHAR;

    SET cpf_constraint_name = (
        SELECT tc.CONSTRAINT_NAME
        FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc
                 JOIN INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE ccu
                      ON tc.CONSTRAINT_SCHEMA = ccu.CONSTRAINT_SCHEMA
                          AND tc.CONSTRAINT_NAME = ccu.CONSTRAINT_NAME
        WHERE tc.TABLE_SCHEMA = 'PUBLIC'
          AND tc.TABLE_NAME = 'PHYSICAL_PERSONS'
          AND tc.CONSTRAINT_TYPE = 'UNIQUE'
          AND ccu.COLUMN_NAME = 'CPF'
    );

    SET cnpj_constraint_name = (
        SELECT tc.CONSTRAINT_NAME
        FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc
                 JOIN INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE ccu
                      ON tc.CONSTRAINT_SCHEMA = ccu.CONSTRAINT_SCHEMA
                          AND tc.CONSTRAINT_NAME = ccu.CONSTRAINT_NAME
        WHERE tc.TABLE_SCHEMA = 'PUBLIC'
          AND tc.TABLE_NAME = 'JURIDICAL_PERSONS'
          AND tc.CONSTRAINT_TYPE = 'UNIQUE'
          AND ccu.COLUMN_NAME = 'CNPJ'
    );

    IF cpf_constraint_name IS NOT NULL THEN
        EXECUTE IMMEDIATE 'ALTER TABLE PHYSICAL_PERSONS DROP CONSTRAINT ' || cpf_constraint_name;
    END IF;

    IF cnpj_constraint_name IS NOT NULL THEN
        EXECUTE IMMEDIATE 'ALTER TABLE JURIDICAL_PERSONS DROP CONSTRAINT ' || cnpj_constraint_name;
    END IF;
END;
