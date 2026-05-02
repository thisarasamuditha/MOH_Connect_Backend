-- ====================================================
-- Setup PHM Areas
-- ====================================================
USE moh_db;

-- Verify table exists
SHOW TABLES LIKE 'PHM_AREA';

-- Clear existing data
TRUNCATE TABLE PHM_AREA;

-- Insert PHM Areas
INSERT INTO PHM_AREA (area_name, area_code) VALUES
('Colombo Central', 'PHM-COL-001'),
('Colombo North', 'PHM-COL-002'),
('Kandy City', 'PHM-KDY-001'),
('Galle Fort', 'PHM-GAL-001'),
('Kurunegala Central', 'PHM-KUR-001');

-- Verify data was inserted
SELECT 'PHM_AREA Setup Complete' AS Status;
SELECT phm_area_id, area_name, area_code FROM PHM_AREA;
