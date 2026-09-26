-- Phase 1 identity, RBAC, mobility catalogue, financial ledger, and journey tables.

CREATE TYPE user_role AS ENUM (
  'STUDENT',
  'CONDUCTOR',
  'DRIVER',
  'OPERATOR_ADMIN',
  'UNIVERSITY_ADMIN',
  'AUDITOR'
);

CREATE TABLE users (
  id UUID PRIMARY KEY,
  institution_id UUID NOT NULL REFERENCES institutions(id),
  email TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  full_name TEXT NOT NULL,
  role user_role NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX users_institution_id_idx ON users (institution_id);

CREATE TABLE sessions (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token TEXT NOT NULL UNIQUE,
  expires_at TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE operators (
  id UUID PRIMARY KEY,
  institution_id UUID NOT NULL REFERENCES institutions(id),
  name TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE routes (
  id UUID PRIMARY KEY,
  institution_id UUID NOT NULL REFERENCES institutions(id),
  name TEXT NOT NULL,
  origin TEXT NOT NULL,
  destination TEXT NOT NULL,
  default_fare_minor INT NOT NULL CHECK (default_fare_minor >= 0),
  currency TEXT NOT NULL DEFAULT 'USD',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE vehicles (
  id UUID PRIMARY KEY,
  operator_id UUID NOT NULL REFERENCES operators(id),
  registration TEXT NOT NULL UNIQUE,
  assigned_route_id UUID REFERENCES routes(id),
  capacity INT NOT NULL DEFAULT 40,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Double-Entry Ledger System
CREATE TABLE ledger_accounts (
  id UUID PRIMARY KEY,
  user_id UUID REFERENCES users(id),
  operator_id UUID REFERENCES operators(id),
  account_type TEXT NOT NULL, -- 'STUDENT_WALLET', 'OPERATOR_PAYABLE', 'SYSTEM_CLEARING'
  balance_minor INT NOT NULL DEFAULT 0,
  currency TEXT NOT NULL DEFAULT 'USD',
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE journal_entries (
  id UUID PRIMARY KEY,
  description TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE ledger_postings (
  id UUID PRIMARY KEY,
  journal_entry_id UUID NOT NULL REFERENCES journal_entries(id),
  account_id UUID NOT NULL REFERENCES ledger_accounts(id),
  amount_minor INT NOT NULL, -- Positive = Credit, Negative = Debit
  currency TEXT NOT NULL DEFAULT 'USD'
);

CREATE TABLE journey_records (
  id UUID PRIMARY KEY,
  institution_id UUID NOT NULL REFERENCES institutions(id),
  student_id UUID NOT NULL REFERENCES users(id),
  conductor_id UUID REFERENCES users(id),
  vehicle_registration TEXT NOT NULL,
  route_name TEXT NOT NULL,
  fare_minor INT NOT NULL,
  currency TEXT NOT NULL DEFAULT 'USD',
  status TEXT NOT NULL, -- 'CONFIRMED', 'PROVISIONAL', 'DECLINED'
  occurred_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX journey_records_student_idx ON journey_records (student_id, occurred_at DESC);
