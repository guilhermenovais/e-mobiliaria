-- 1. Add nullable column
ALTER TABLE receipts
    ADD COLUMN payment_due_date DATE;

-- 2. Backfill: use paymentDay clamped to the month of interval_start
UPDATE receipts r
SET payment_due_date = (SELECT CASE
                                   WHEN c.payment_day <=
                                       DAY (DATEADD('DAY', -1, DATEADD('MONTH', 1,
                                       DATEADD('DAY', -(DAY (r.interval_start)-1), r.interval_start)))) THEN DATEADD('DAY', c.payment_day - 1,
                      DATEADD('DAY', -(DAY(r.interval_start)-1), r.interval_start))
             ELSE DATEADD('DAY', -1, DATEADD('MONTH', 1,
                      DATEADD('DAY', -(DAY(r.interval_start)-1), r.interval_start)))
END
FROM contracts c
    WHERE c.id = r.contract_id
);

-- 3. Make non-nullable
ALTER TABLE receipts ALTER COLUMN payment_due_date DATE NOT NULL;

-- 4. Enforce uniqueness
ALTER TABLE receipts
    ADD CONSTRAINT uq_receipt_contract_payment_due_date
        UNIQUE (contract_id, payment_due_date);
