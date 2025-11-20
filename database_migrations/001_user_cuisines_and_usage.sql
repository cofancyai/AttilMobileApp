-- Migration: Add support for multiple cuisines per user and usage_id in indents
-- Run this SQL in your Supabase SQL Editor

-- 1. Create user_cuisines junction table for many-to-many relationship
CREATE TABLE IF NOT EXISTS user_cuisines (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    cuisine_id UUID NOT NULL REFERENCES cuisines(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, cuisine_id)
);

-- Add index for faster lookups
CREATE INDEX IF NOT EXISTS idx_user_cuisines_user_id ON user_cuisines(user_id);
CREATE INDEX IF NOT EXISTS idx_user_cuisines_cuisine_id ON user_cuisines(user_id);

-- 2. Migrate existing user cuisine assignments to junction table
INSERT INTO user_cuisines (user_id, cuisine_id)
SELECT id, cuisine_id
FROM users
WHERE cuisine_id IS NOT NULL
ON CONFLICT (user_id, cuisine_id) DO NOTHING;

-- 3. Add usage_id to indents table (if not exists)
ALTER TABLE indents
ADD COLUMN IF NOT EXISTS usage_id UUID REFERENCES usages(id);

-- 4. Migrate existing purpose text to usage_id if possible
-- First, find or create corresponding usage records
-- This is optional - only if you want to migrate existing purposes

-- 5. Add index for indent queries
CREATE INDEX IF NOT EXISTS idx_indents_usage_id ON indents(usage_id);
CREATE INDEX IF NOT EXISTS idx_indents_chef_id ON indents(chef_id);

-- 6. IMPORTANT: Reload PostgREST schema cache
NOTIFY pgrst, 'reload schema';

-- Note: The users.cuisine_id field is kept for backward compatibility
-- but the primary source of user cuisines should now be the user_cuisines table

COMMENT ON TABLE user_cuisines IS 'Junction table for many-to-many relationship between users and cuisines';
COMMENT ON COLUMN indents.usage_id IS 'Reference to usage table for purpose classification';
