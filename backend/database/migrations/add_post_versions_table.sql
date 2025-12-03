-- Migration: Add post_versions table
-- Run this on your database to add support for post version history

CREATE TABLE IF NOT EXISTS post_versions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    version_number INTEGER NOT NULL,
    title VARCHAR(500) NOT NULL,
    content TEXT,
    prompt_section TEXT,
    description_section TEXT,
    llm_tag VARCHAR(100) NOT NULL,
    is_prompt_post BOOLEAN DEFAULT FALSE,
    anonymous BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(post_id, version_number)
);

CREATE INDEX IF NOT EXISTS idx_post_versions_post ON post_versions(post_id);
CREATE INDEX IF NOT EXISTS idx_post_versions_created_at ON post_versions(created_at DESC);

