-- Migration: Add title field to comments table and ensure created_at is properly indexed
-- Run this on your database to add support for comment titles

-- Add title column (optional, can be NULL)
ALTER TABLE comments ADD COLUMN IF NOT EXISTS title VARCHAR(500);

-- Ensure created_at index exists for better query performance
CREATE INDEX IF NOT EXISTS idx_comments_created_at ON comments(created_at DESC);

