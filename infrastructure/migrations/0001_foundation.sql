-- Phase 1 baseline. Apply through the selected migration runner, never manually in production.
CREATE TABLE institutions (
  id UUID PRIMARY KEY,
  code TEXT NOT NULL UNIQUE,
  name TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE audit_events (
  id UUID PRIMARY KEY,
  institution_id UUID NOT NULL REFERENCES institutions(id),
  actor_id UUID NOT NULL,
  event_type TEXT NOT NULL,
  target_type TEXT NOT NULL,
  target_id TEXT NOT NULL,
  request_id TEXT NOT NULL,
  occurred_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  before_state JSONB,
  after_state JSONB
);
CREATE INDEX audit_events_institution_occurred_at_idx ON audit_events (institution_id, occurred_at DESC);
