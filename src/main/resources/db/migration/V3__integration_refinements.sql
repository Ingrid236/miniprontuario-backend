-- V3: Backend Integration Refinements
-- Add medications column to patient table
ALTER TABLE patient ADD COLUMN IF NOT EXISTS medications TEXT;

-- Add status and cost columns to procedure table
ALTER TABLE procedure ADD COLUMN IF NOT EXISTS status VARCHAR(50) NOT NULL DEFAULT 'PLANNED';
ALTER TABLE procedure ADD COLUMN IF NOT EXISTS cost NUMERIC(10, 2);

-- Create refresh_token table
CREATE TABLE IF NOT EXISTS refresh_token (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token VARCHAR(512) NOT NULL UNIQUE,
    dentist_id UUID NOT NULL REFERENCES dentist(id) ON DELETE CASCADE,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_refresh_token_token ON refresh_token(token);
CREATE INDEX IF NOT EXISTS idx_refresh_token_dentist_id ON refresh_token(dentist_id);
