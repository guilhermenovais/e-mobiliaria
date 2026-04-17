CREATE TABLE inflation_indexes (
  index_type   VARCHAR(10)      NOT NULL,
  year_month   DATE             NOT NULL,
  monthly_rate DOUBLE PRECISION NOT NULL,
  PRIMARY KEY (index_type, year_month)
);
