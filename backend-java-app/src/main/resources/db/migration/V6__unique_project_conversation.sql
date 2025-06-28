CREATE UNIQUE INDEX IF NOT EXISTS ux_conversations_project_active
    ON conversations(project_id)
    WHERE project_id IS NOT NULL AND deleted_at IS NULL;

