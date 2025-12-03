-- Migration: Add anonymous field to posts table
-- Run this on your database to add support for anonymous posting

-- Add anonymous column (defaults to false for existing posts)
ALTER TABLE posts ADD COLUMN IF NOT EXISTS anonymous BOOLEAN DEFAULT FALSE;

