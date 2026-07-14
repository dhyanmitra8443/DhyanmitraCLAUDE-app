-- Same reasoning as V3 for subscription_plans.currency: CHAR(3) and
-- VARCHAR(3) are distinct, strictly-compared JDBC types that Hibernate's
-- schema validator rejects a mismatch on, and there's no good reason for a
-- currency code to be fixed-width. Safe, lossless narrowing.
ALTER TABLE orders ALTER COLUMN currency TYPE VARCHAR(3);
ALTER TABLE payments ALTER COLUMN currency TYPE VARCHAR(3);
