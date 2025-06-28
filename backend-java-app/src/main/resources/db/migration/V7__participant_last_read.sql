ALTER TABLE conversation_participants
    ADD COLUMN IF NOT EXISTS last_read_at TIMESTAMPTZ;
