-- Migration: Add section_type column to SECTION table
-- This migration adds support for section types (Health Center, Clinic, etc.)

ALTER TABLE SECTION ADD COLUMN section_type VARCHAR(50) AFTER section_name;

-- Optional: Add index for section_type for better query performance
CREATE INDEX idx_section_type ON SECTION(section_type);
