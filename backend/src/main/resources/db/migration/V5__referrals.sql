-- Member referrals: a logged-in STUDENT or INSTRUCTOR refers a prospective
-- member by name + email; the referee receives a single-use, expiring link
-- (same mechanics as instructor_invitations) and becomes a STUDENT on accept.
--
-- Mirrors instructor_invitations, with two additions:
--   referrer_id       - who sent the referral (drives the referrer's own list)
--   referred_user_id  - the account created on acceptance (drives "current status")
CREATE TABLE referrals (
  id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  referrer_id        UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  referee_email      CITEXT NOT NULL,
  referee_first_name VARCHAR(100) NOT NULL,
  referee_last_name  VARCHAR(100) NOT NULL,
  token_hash         TEXT NOT NULL,
  status             VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACCEPTED', 'EXPIRED')),
  -- Set only when the referee accepts and their STUDENT account is created;
  -- ON DELETE SET NULL so removing a user never blocks on referral history.
  referred_user_id   UUID REFERENCES users(id) ON DELETE SET NULL,
  expires_at         TIMESTAMPTZ NOT NULL,
  accepted_at        TIMESTAMPTZ,
  created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT referrals_token_unique UNIQUE (token_hash)
);

-- Referrer's own list ("who have I referred") and the admin cross-referrer view.
CREATE INDEX idx_referrals_referrer ON referrals(referrer_id);
CREATE INDEX idx_referrals_referee_email ON referrals(referee_email);
