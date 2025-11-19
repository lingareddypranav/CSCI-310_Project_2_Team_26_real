-- Seed data for development/testing
-- Optional: Insert sample data for testing

-- Sample tags (common LLM tags)
INSERT INTO tags (tag_name) VALUES 
    ('ChatGPT'),
    ('GPT-4'),
    ('GPT-3.5'),
    ('Claude'),
    ('Gemini'),
    ('LLaMA'),
    ('Codex'),
    ('Other')
ON CONFLICT (tag_name) DO NOTHING;

