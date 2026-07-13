-- =============================================================================
-- Dhyan Mitra LMS - Initial Schema
-- Flyway migration V1 (Ref: SRS 2.6, 17.8/19.8 - Flyway-only schema management)
-- Target: PostgreSQL. All primary keys are UUIDs (Ref: SRS 2.6).
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto; -- provides gen_random_uuid() on older PG versions
CREATE EXTENSION IF NOT EXISTS citext;   -- case-insensitive email columns

-- -----------------------------------------------------------------------------
-- Shared trigger: auto-maintain updated_at on every table that has one
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- -----------------------------------------------------------------------------
-- Shared trigger: block UPDATE/DELETE on tables the SRS declares immutable
-- (audit_logs, certificates - Ref: SRS 3.17/4.17/etc., 12.11)
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION reject_mutation()
RETURNS TRIGGER AS $$
BEGIN
  RAISE EXCEPTION '% is immutable: % is not permitted on %', TG_TABLE_NAME, TG_OP, TG_TABLE_NAME;
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- Chapter 3/4: Authentication & Authorization, User Management
-- =============================================================================
CREATE TABLE users (
  id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  first_name            VARCHAR(100) NOT NULL,
  last_name             VARCHAR(100) NOT NULL,
  email                 CITEXT NOT NULL,
  mobile_number         VARCHAR(20) NOT NULL,
  password_hash         TEXT NOT NULL,
  role                  VARCHAR(20) NOT NULL CHECK (role IN ('ADMINISTRATOR', 'INSTRUCTOR', 'STUDENT')),
  status                VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'BLOCKED')),
  profile_photo_url     TEXT,
  date_of_birth         DATE,
  gender                VARCHAR(30),
  -- Instructor-only fields (Ref: SRS 4.7):
  professional_bio      TEXT,
  years_of_experience   INT,
  created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT users_email_unique UNIQUE (email),
  CONSTRAINT users_mobile_unique UNIQUE (mobile_number)
);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(status);
CREATE TRIGGER trg_users_updated_at BEFORE UPDATE ON users
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Ref: SRS 4.7 (instructor specializations, e.g. "Yoga", "Meditation")
CREATE TABLE instructor_specializations (
  user_id         UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  specialization  VARCHAR(100) NOT NULL,
  PRIMARY KEY (user_id, specialization)
);

-- Ref: SRS 3.5 - instructor invitation flow
CREATE TABLE instructor_invitations (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email         CITEXT NOT NULL,
  first_name    VARCHAR(100) NOT NULL,
  last_name     VARCHAR(100) NOT NULL,
  token_hash    TEXT NOT NULL,
  status        VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACCEPTED', 'EXPIRED')),
  invited_by    UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  expires_at    TIMESTAMPTZ NOT NULL,
  accepted_at   TIMESTAMPTZ,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT instructor_invitations_token_unique UNIQUE (token_hash)
);
CREATE INDEX idx_instructor_invitations_email ON instructor_invitations(email);

-- Ref: SRS 3.7, 3.14 - single active session per user
CREATE TABLE user_sessions (
  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id        UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  device_info    TEXT,
  ip_address     INET,
  status         VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
  login_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  logout_at      TIMESTAMPTZ
);
-- Enforces "only one active session per user" (Ref: SRS 3.7) at the DB level.
CREATE UNIQUE INDEX idx_user_sessions_one_active ON user_sessions(user_id) WHERE status = 'ACTIVE';

-- Ref: SRS 3.10, 3.12 - refresh tokens and password-reset tokens (unified: same shape, single-use)
CREATE TABLE auth_tokens (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id       UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  session_id    UUID REFERENCES user_sessions(id) ON DELETE SET NULL, -- set for REFRESH tokens only
  token_type    VARCHAR(20) NOT NULL CHECK (token_type IN ('REFRESH', 'PASSWORD_RESET')),
  token_hash    TEXT NOT NULL,
  expires_at    TIMESTAMPTZ NOT NULL,
  used_at       TIMESTAMPTZ,
  revoked_at    TIMESTAMPTZ,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT auth_tokens_token_hash_unique UNIQUE (token_hash)
);
CREATE INDEX idx_auth_tokens_user_type ON auth_tokens(user_id, token_type);

-- Ref: SRS 3.17, 4.17, 5.19, etc. - unified, immutable audit log across every module
CREATE TABLE audit_logs (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id           UUID REFERENCES users(id) ON DELETE SET NULL,
  action            VARCHAR(100) NOT NULL,
  entity_type       VARCHAR(100) NOT NULL,
  entity_id         UUID,
  previous_values   JSONB,
  updated_values    JSONB,
  ip_address        INET,
  created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
CREATE TRIGGER trg_audit_logs_immutable BEFORE UPDATE OR DELETE ON audit_logs
  FOR EACH ROW EXECUTE FUNCTION reject_mutation();

-- =============================================================================
-- Chapter 6: Category Management
-- =============================================================================
CREATE TABLE categories (
  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name           VARCHAR(150) NOT NULL,
  description    TEXT,
  icon_url       TEXT,
  display_order  INT,
  status         VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
  created_by     UUID REFERENCES users(id) ON DELETE SET NULL,
  updated_by     UUID REFERENCES users(id) ON DELETE SET NULL,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT categories_name_unique UNIQUE (name)
);
CREATE INDEX idx_categories_status ON categories(status);
CREATE TRIGGER trg_categories_updated_at BEFORE UPDATE ON categories
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- =============================================================================
-- Chapter 5: Course Management
-- =============================================================================
CREATE TABLE courses (
  id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  title                       VARCHAR(200) NOT NULL,
  short_description           TEXT NOT NULL,
  detailed_description        TEXT NOT NULL,
  thumbnail_url               TEXT NOT NULL,
  language                    VARCHAR(50) NOT NULL,
  difficulty_level            VARCHAR(20) NOT NULL CHECK (difficulty_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
  estimated_duration_minutes  INT,
  status                      VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
  preview_lesson_id           UUID, -- FK added after `lessons` exists (circular dependency, see below)
  published_at                TIMESTAMPTZ,
  created_at                  TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at                  TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT courses_title_unique UNIQUE (title)
);
CREATE INDEX idx_courses_status ON courses(status);
CREATE INDEX idx_courses_difficulty ON courses(difficulty_level);
CREATE TRIGGER trg_courses_updated_at BEFORE UPDATE ON courses
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Ref: SRS 5.6 - many-to-many course<->instructor
CREATE TABLE course_instructors (
  course_id       UUID NOT NULL REFERENCES courses(id) ON DELETE RESTRICT,
  instructor_id   UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  assigned_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (course_id, instructor_id)
);
CREATE INDEX idx_course_instructors_instructor ON course_instructors(instructor_id);

-- Ref: SRS 5.7, 6.6 - many-to-many course<->category
CREATE TABLE course_categories (
  course_id     UUID NOT NULL REFERENCES courses(id) ON DELETE RESTRICT,
  category_id   UUID NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
  PRIMARY KEY (course_id, category_id)
);
CREATE INDEX idx_course_categories_category ON course_categories(category_id);

-- =============================================================================
-- Chapter 7: Section & Lesson Management
-- =============================================================================
CREATE TABLE sections (
  id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  course_id          UUID NOT NULL REFERENCES courses(id) ON DELETE RESTRICT,
  title              VARCHAR(200) NOT NULL,
  short_description  TEXT,
  display_order      INT NOT NULL,
  status             VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
  created_by         UUID REFERENCES users(id) ON DELETE SET NULL,
  updated_by         UUID REFERENCES users(id) ON DELETE SET NULL,
  created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT sections_course_title_unique UNIQUE (course_id, title),
  CONSTRAINT sections_course_order_unique UNIQUE (course_id, display_order)
);
CREATE TRIGGER trg_sections_updated_at BEFORE UPDATE ON sections
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE lessons (
  id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  section_id              UUID NOT NULL REFERENCES sections(id) ON DELETE RESTRICT,
  -- Denormalized from sections.course_id: lessons cannot move between courses
  -- (Ref: SRS 7.3), which makes this safe to set once at creation and never
  -- change. Needed so the "one preview lesson per course" constraint below
  -- can be enforced with a plain index instead of a cross-table trigger.
  course_id               UUID NOT NULL REFERENCES courses(id) ON DELETE RESTRICT,
  title                   VARCHAR(200) NOT NULL,
  detailed_description    TEXT,
  video_url               TEXT NOT NULL,
  video_duration_seconds  INT,
  thumbnail_url           TEXT,
  display_order           INT NOT NULL,
  status                  VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
  is_preview              BOOLEAN NOT NULL DEFAULT FALSE,
  created_by              UUID REFERENCES users(id) ON DELETE SET NULL,
  updated_by              UUID REFERENCES users(id) ON DELETE SET NULL,
  created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT lessons_section_title_unique UNIQUE (section_id, title),
  CONSTRAINT lessons_section_order_unique UNIQUE (section_id, display_order)
);
CREATE INDEX idx_lessons_course ON lessons(course_id);
-- Enforces "exactly one preview lesson per course" (Ref: SRS 7.11) at the DB level.
CREATE UNIQUE INDEX idx_lessons_one_preview_per_course ON lessons(course_id) WHERE is_preview = TRUE;
CREATE TRIGGER trg_lessons_updated_at BEFORE UPDATE ON lessons
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Deferred FK to close the courses <-> lessons circular reference.
ALTER TABLE courses
  ADD CONSTRAINT courses_preview_lesson_fk
  FOREIGN KEY (preview_lesson_id) REFERENCES lessons(id) ON DELETE SET NULL;

-- =============================================================================
-- Chapter 8: Lesson Resources & File Management
-- =============================================================================
CREATE TABLE lesson_resources (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  lesson_id       UUID NOT NULL REFERENCES lessons(id) ON DELETE RESTRICT,
  resource_type   VARCHAR(20) NOT NULL CHECK (resource_type IN ('VIDEO', 'PDF', 'IMAGE', 'AUDIO', 'ZIP', 'EXTERNAL_LINK')),
  display_name    VARCHAR(200) NOT NULL,
  description     TEXT,
  external_url    TEXT, -- required for VIDEO / EXTERNAL_LINK
  file_reference  TEXT, -- required for PDF / IMAGE / AUDIO / ZIP
  file_name       TEXT,
  file_type       VARCHAR(100),
  file_size_bytes BIGINT,
  display_order   INT NOT NULL,
  status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED')),
  created_by      UUID REFERENCES users(id) ON DELETE SET NULL,
  updated_by      UUID REFERENCES users(id) ON DELETE SET NULL,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT lesson_resources_order_unique UNIQUE (lesson_id, display_order),
  CONSTRAINT lesson_resources_url_or_file CHECK (
    (resource_type IN ('VIDEO', 'EXTERNAL_LINK') AND external_url IS NOT NULL)
    OR
    (resource_type IN ('PDF', 'IMAGE', 'AUDIO', 'ZIP') AND file_reference IS NOT NULL)
  )
);
-- Enforces "exactly one active VIDEO resource per lesson" (Ref: SRS 8.3, 8.6).
CREATE UNIQUE INDEX idx_lesson_resources_one_active_video
  ON lesson_resources(lesson_id) WHERE resource_type = 'VIDEO' AND status = 'ACTIVE';
CREATE TRIGGER trg_lesson_resources_updated_at BEFORE UPDATE ON lesson_resources
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- =============================================================================
-- Chapter 9: Subscription Plans & Student Enrollments
-- =============================================================================
CREATE TABLE subscription_plans (
  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  course_id      UUID NOT NULL REFERENCES courses(id) ON DELETE RESTRICT,
  plan_name      VARCHAR(100) NOT NULL,
  description    TEXT,
  price          NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
  currency       CHAR(3) NOT NULL DEFAULT 'INR',
  duration       INT NOT NULL CHECK (duration > 0),
  duration_unit  VARCHAR(10) NOT NULL CHECK (duration_unit IN ('DAY', 'MONTH', 'YEAR')),
  status         VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED')),
  created_by     UUID REFERENCES users(id) ON DELETE SET NULL,
  updated_by     UUID REFERENCES users(id) ON DELETE SET NULL,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_subscription_plans_course ON subscription_plans(course_id);
CREATE TRIGGER trg_subscription_plans_updated_at BEFORE UPDATE ON subscription_plans
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Ref: SRS 9.11 - a renewal EXTENDS the existing subscription's end_date rather
-- than creating a new row, so there is exactly one (evolving) subscription
-- record per student+course, distinct from the append-only orders/payments
-- history below.
CREATE TABLE subscriptions (
  id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  student_id             UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  course_id              UUID NOT NULL REFERENCES courses(id) ON DELETE RESTRICT,
  subscription_plan_id   UUID NOT NULL REFERENCES subscription_plans(id) ON DELETE RESTRICT,
  start_date             DATE NOT NULL,
  end_date               DATE NOT NULL,
  status                 VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('ACTIVE', 'EXPIRED', 'CANCELLED', 'PENDING')),
  purchase_date          TIMESTAMPTZ,
  created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT subscriptions_student_course_unique UNIQUE (student_id, course_id)
);
CREATE INDEX idx_subscriptions_status ON subscriptions(status);
CREATE TRIGGER trg_subscriptions_updated_at BEFORE UPDATE ON subscriptions
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- =============================================================================
-- Chapter 10: Payment Management (incl. 10.12 Razorpay Integration)
-- =============================================================================
CREATE TABLE orders (
  id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  student_id             UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  course_id              UUID NOT NULL REFERENCES courses(id) ON DELETE RESTRICT,
  subscription_plan_id   UUID NOT NULL REFERENCES subscription_plans(id) ON DELETE RESTRICT,
  amount                 NUMERIC(12, 2) NOT NULL CHECK (amount >= 0),
  currency               CHAR(3) NOT NULL DEFAULT 'INR',
  status                 VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PAID', 'FAILED', 'CANCELLED')),
  created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at             TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_orders_student ON orders(student_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE TRIGGER trg_orders_updated_at BEFORE UPDATE ON orders
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Ref: SRS 10.3 - an order may have multiple payment attempts (failed
-- retries), but at most one SUCCESS payment; enforced via the partial
-- unique index below rather than a UNIQUE(order_id) on the whole table.
CREATE TABLE payments (
  id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  order_id                UUID NOT NULL REFERENCES orders(id) ON DELETE RESTRICT,
  student_id              UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  amount                  NUMERIC(12, 2) NOT NULL CHECK (amount >= 0),
  currency                CHAR(3) NOT NULL DEFAULT 'INR',
  payment_method          VARCHAR(20) CHECK (payment_method IN ('CREDIT_CARD', 'DEBIT_CARD', 'UPI', 'NET_BANKING', 'WALLET')),
  transaction_reference   TEXT, -- Razorpay payment ID (Ref: SRS 10.11)
  razorpay_order_id       TEXT,
  status                  VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'CANCELLED')),
  payment_date            TIMESTAMPTZ,
  created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT payments_transaction_reference_unique UNIQUE (transaction_reference)
);
CREATE UNIQUE INDEX idx_payments_one_success_per_order ON payments(order_id) WHERE status = 'SUCCESS';
CREATE INDEX idx_payments_student ON payments(student_id);
CREATE TRIGGER trg_payments_updated_at BEFORE UPDATE ON payments
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Ref: SRS 10.12 (Idempotency) - the unique constraint on razorpay_event_id
-- is what makes webhook replay safe: a duplicate delivery simply fails to
-- insert and is treated as a no-op by the application.
CREATE TABLE razorpay_webhook_events (
  id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  razorpay_event_id  TEXT NOT NULL,
  event_type         VARCHAR(100) NOT NULL,
  payload            JSONB NOT NULL,
  processed_at       TIMESTAMPTZ,
  created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT razorpay_webhook_events_event_id_unique UNIQUE (razorpay_event_id)
);

-- =============================================================================
-- Chapter 11: Live Classes
-- =============================================================================
CREATE TABLE live_classes (
  id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  course_id          UUID NOT NULL REFERENCES courses(id) ON DELETE RESTRICT,
  title              VARCHAR(200) NOT NULL,
  description        TEXT,
  scheduled_date     DATE NOT NULL,
  scheduled_time     TIME NOT NULL,
  meeting_url        TEXT NOT NULL,
  meeting_password   TEXT,
  -- Recording URL must satisfy the same private/unlisted, domain-restricted
  -- hosting requirement as lesson videos (Ref: SRS 7.8, 11.11).
  recording_url      TEXT,
  status             VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED' CHECK (status IN ('SCHEDULED', 'CANCELLED', 'COMPLETED')),
  created_by         UUID REFERENCES users(id) ON DELETE SET NULL,
  updated_by         UUID REFERENCES users(id) ON DELETE SET NULL,
  created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_live_classes_course ON live_classes(course_id);
CREATE INDEX idx_live_classes_scheduled_date ON live_classes(scheduled_date);
CREATE TRIGGER trg_live_classes_updated_at BEFORE UPDATE ON live_classes
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE live_class_attendance (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  live_class_id   UUID NOT NULL REFERENCES live_classes(id) ON DELETE RESTRICT,
  student_id      UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  joined_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT live_class_attendance_unique UNIQUE (live_class_id, student_id)
);

-- =============================================================================
-- Chapter 12: Student Progress Tracking & Certificate Management
-- =============================================================================
CREATE TABLE lesson_progress (
  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  student_id     UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  lesson_id      UUID NOT NULL REFERENCES lessons(id) ON DELETE RESTRICT,
  status         VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED' CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED')),
  started_at     TIMESTAMPTZ,
  completed_at   TIMESTAMPTZ,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT lesson_progress_student_lesson_unique UNIQUE (student_id, lesson_id)
);
CREATE INDEX idx_lesson_progress_student ON lesson_progress(student_id);
CREATE TRIGGER trg_lesson_progress_updated_at BEFORE UPDATE ON lesson_progress
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Course-level completion fact, derived from lesson_progress but persisted
-- so course completion timestamps and certificate triggers don't require
-- recomputation (Ref: SRS 12.9).
CREATE TABLE course_completions (
  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  student_id     UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  course_id      UUID NOT NULL REFERENCES courses(id) ON DELETE RESTRICT,
  completed_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT course_completions_student_course_unique UNIQUE (student_id, course_id)
);

-- Ref: SRS 12.11 - immutable after issuance. verification_id is the
-- non-guessable identifier used by the public verification endpoint
-- (Ref: SRS 12.13, fixed 2026-07-13 to be publicly verifiable).
CREATE TABLE certificates (
  id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  certificate_number          TEXT NOT NULL,
  verification_id             UUID NOT NULL DEFAULT gen_random_uuid(),
  student_id                  UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  course_id                   UUID NOT NULL REFERENCES courses(id) ON DELETE RESTRICT,
  completion_date             DATE NOT NULL,
  issue_date                  DATE NOT NULL DEFAULT CURRENT_DATE,
  -- Snapshotted at issuance so later org/instructor changes never alter an
  -- already-issued certificate (Ref: SRS 12.11 immutability).
  organization_name_snapshot  TEXT NOT NULL,
  instructor_names_snapshot   TEXT[] NOT NULL,
  file_reference              TEXT,
  created_at                  TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT certificates_number_unique UNIQUE (certificate_number),
  CONSTRAINT certificates_verification_id_unique UNIQUE (verification_id),
  CONSTRAINT certificates_student_course_unique UNIQUE (student_id, course_id)
);
CREATE TRIGGER trg_certificates_immutable BEFORE UPDATE OR DELETE ON certificates
  FOR EACH ROW EXECUTE FUNCTION reject_mutation();

-- =============================================================================
-- Chapter 14: Notification Management
-- =============================================================================
CREATE TABLE notifications (
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  recipient_user_id   UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  title               VARCHAR(200) NOT NULL,
  message             TEXT NOT NULL,
  notification_type   VARCHAR(20) NOT NULL CHECK (notification_type IN ('IN_APP', 'EMAIL')),
  related_module      VARCHAR(100),
  related_entity_id   UUID,
  read_status         VARCHAR(10) NOT NULL DEFAULT 'UNREAD' CHECK (read_status IN ('UNREAD', 'READ')),
  created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
  read_at             TIMESTAMPTZ
);
CREATE INDEX idx_notifications_recipient_status ON notifications(recipient_user_id, read_status);

-- =============================================================================
-- Chapter 15: Reports Management (async export job tracking)
-- =============================================================================
CREATE TABLE report_exports (
  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  report_key     VARCHAR(50) NOT NULL,
  requested_by   UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  format         VARCHAR(10) NOT NULL CHECK (format IN ('PDF', 'XLSX', 'CSV')),
  status         VARCHAR(20) NOT NULL DEFAULT 'PROCESSING' CHECK (status IN ('PROCESSING', 'READY', 'FAILED')),
  file_reference TEXT,
  requested_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  completed_at   TIMESTAMPTZ
);

-- =============================================================================
-- Chapter 16: System Settings (singleton table - Ref: SRS 16.1, centralized config)
-- =============================================================================
CREATE TABLE system_settings (
  id                                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  -- The unique constraint on this always-true column is what makes the
  -- table a true singleton: a second INSERT will always violate it.
  singleton                        BOOLEAN NOT NULL DEFAULT TRUE,
  organization_name                 VARCHAR(200),
  organization_logo_url             TEXT,
  support_email                     CITEXT,
  support_phone                     VARCHAR(20),
  website_url                       TEXT,
  organization_address              TEXT,
  session_timeout_minutes           INT,
  max_login_attempts                INT,
  -- Ref: SRS 3.9/16.5 fix (2026-07-13): 3.9's defaults are bounded here -
  -- length can only be raised (8-64), and at least one letter class plus
  -- one digit-or-special requirement must always stay mandatory.
  password_min_length               INT NOT NULL DEFAULT 8 CHECK (password_min_length BETWEEN 8 AND 64),
  password_require_uppercase        BOOLEAN NOT NULL DEFAULT TRUE,
  password_require_lowercase        BOOLEAN NOT NULL DEFAULT TRUE,
  password_require_digit            BOOLEAN NOT NULL DEFAULT TRUE,
  password_require_special_char     BOOLEAN NOT NULL DEFAULT TRUE,
  max_upload_size_mb                INT,
  allowed_file_types                TEXT[],
  max_files_per_upload               INT,
  smtp_host                         TEXT,
  smtp_port                         INT,
  sender_email                      CITEXT,
  sender_display_name               VARCHAR(200),
  smtp_username                      TEXT,
  smtp_password_encrypted           TEXT,
  encryption_type                   VARCHAR(10) CHECK (encryption_type IN ('SSL', 'TLS')),
  email_notifications_enabled       BOOLEAN NOT NULL DEFAULT TRUE,
  in_app_notifications_enabled      BOOLEAN NOT NULL DEFAULT TRUE,
  razorpay_key_id                   TEXT,
  razorpay_key_secret_encrypted     TEXT,
  razorpay_webhook_secret_encrypted TEXT,
  webhook_callback_url              TEXT,
  environment                       VARCHAR(10) NOT NULL DEFAULT 'SANDBOX' CHECK (environment IN ('SANDBOX', 'PRODUCTION')),
  default_time_zone                 VARCHAR(50) DEFAULT 'Asia/Kolkata',
  default_meeting_duration_minutes  INT,
  default_reminder_minutes_before   INT,
  certificate_organization_name     VARCHAR(200),
  certificate_logo_url              TEXT,
  certificate_signature_url         TEXT,
  certificate_footer_text           TEXT,
  maintenance_mode_enabled          BOOLEAN NOT NULL DEFAULT FALSE,
  backup_location                   TEXT,
  backup_frequency                  VARCHAR(10) CHECK (backup_frequency IN ('DAILY', 'WEEKLY')),
  backup_retention_days             INT,
  updated_by                        UUID REFERENCES users(id) ON DELETE SET NULL,
  updated_at                        TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT system_settings_singleton_unique UNIQUE (singleton),
  CONSTRAINT system_settings_singleton_check CHECK (singleton = TRUE),
  -- Ref: SRS 16.5 fix: at least one letter class AND one digit-or-special
  -- requirement must remain mandatory; complexity can never be fully disabled.
  CONSTRAINT system_settings_password_complexity_floor CHECK (
    (password_require_uppercase OR password_require_lowercase)
    AND
    (password_require_digit OR password_require_special_char)
  )
);
CREATE TRIGGER trg_system_settings_updated_at BEFORE UPDATE ON system_settings
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Seed the single settings row (Ref: SRS 16 - configuration must exist from day one).
INSERT INTO system_settings (organization_name) VALUES ('Dhyan Mitra');
