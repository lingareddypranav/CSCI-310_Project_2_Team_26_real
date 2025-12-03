-- Add anonymous flag to posts so users can hide their displayed name
ALTER TABLE posts ADD COLUMN IF NOT EXISTS anonymous BOOLEAN DEFAULT FALSE;

-- Backfill any null values that may exist if the column was added without a default
UPDATE posts SET anonymous = FALSE WHERE anonymous IS NULL;
