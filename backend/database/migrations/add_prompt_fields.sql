-- Migration: Add prompt_section and description_section to posts table
-- Run this on your database to add support for prompt posts with separate sections

-- Add prompt_section column (for the actual prompt text)
ALTER TABLE posts ADD COLUMN IF NOT EXISTS prompt_section TEXT;

-- Add description_section column (for description/context about the prompt)
ALTER TABLE posts ADD COLUMN IF NOT EXISTS description_section TEXT;

-- Update existing prompt posts: if content exists, move it to description_section
-- (This is a one-time migration for existing data)
UPDATE posts 
SET description_section = content 
WHERE is_prompt_post = TRUE AND description_section IS NULL;

