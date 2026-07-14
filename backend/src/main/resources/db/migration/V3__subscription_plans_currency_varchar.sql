-- CHAR(3) and VARCHAR(3) are both strictly-typed, distinct JDBC types
-- (Types.CHAR vs Types.VARCHAR) that Hibernate's schema validator compares
-- exactly - unlike citext/inet (which Postgres's JDBC driver reports as the
-- looser Types.OTHER), no entity-side columnDefinition hint bypasses this
-- mismatch. VARCHAR is also the more conventional choice for a currency
-- code with no real reason to be fixed-width. Safe, lossless narrowing:
-- existing values are already exactly 3 characters.
ALTER TABLE subscription_plans ALTER COLUMN currency TYPE VARCHAR(3);
