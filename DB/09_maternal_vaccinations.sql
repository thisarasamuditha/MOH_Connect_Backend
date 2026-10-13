-- =====================================================================
-- 09_maternal_vaccinations.sql
-- Client request 3.3 - vaccination records for pregnant mothers (TT/Td).
--   * MOTHER: earlier tetanus doses received before they were recorded here
--   * VACCINE_SCHEDULE: tetanus toxoid doses 4 and 5 for mothers
--
-- RUN AFTER 07_clinical_workflows.sql (it adds VACCINE_SCHEDULE.is_active)
-- and BEFORE DEPLOYING the backend build that contains this feature:
-- the backend maps the new MOTHER columns, so every MOTHER query fails
-- until they exist.
--
-- Safe to run more than once (IF NOT EXISTS / NOT EXISTS guards).
-- Take a backup first.
-- =====================================================================

USE moh_db;

-- ---------------------------------------------------------------------
-- 1. MOTHER: tetanus history from before this system
--    NULL = the midwife has not recorded it yet.
-- ---------------------------------------------------------------------
ALTER TABLE MOTHER ADD COLUMN IF NOT EXISTS prior_tt_doses INT NULL;
ALTER TABLE MOTHER ADD COLUMN IF NOT EXISTS prior_tt_last_date DATE NULL;

-- ---------------------------------------------------------------------
-- 2. MOTHER_VACCINATION (already created by 03_vaccination_schema.sql
--    on most databases; created here only if missing)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS MOTHER_VACCINATION (
    vaccination_id INT PRIMARY KEY AUTO_INCREMENT,
    pregnancy_id INT NOT NULL,
    midwife_id INT,
    schedule_id INT NOT NULL,
    vaccination_date DATE NOT NULL,
    batch_number VARCHAR(100),
    manufacturer VARCHAR(255),
    next_dose_date DATE,
    adverse_reaction TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pregnancy_id) REFERENCES PREGNANCY(pregnancy_id) ON DELETE CASCADE,
    FOREIGN KEY (midwife_id) REFERENCES MIDWIFE(midwife_id) ON DELETE SET NULL,
    FOREIGN KEY (schedule_id) REFERENCES VACCINE_SCHEDULE(schedule_id) ON DELETE RESTRICT,
    INDEX idx_pregnancy (pregnancy_id),
    INDEX idx_vaccination_date (vaccination_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 3. Tetanus toxoid doses 4 and 5 for mothers (doses 1-3 already exist).
--    The backend works out which dose a mother needs from her history;
--    the dose number here is the lifetime dose number.
--    >>> To be verified and signed off by the MOH / Medical Officer. <<<
-- ---------------------------------------------------------------------
INSERT INTO VACCINE_SCHEDULE
    (vaccine_name, target_group, dose_number, recommended_age_days, description,
     is_active, is_conditional, target_gender, created_at)
SELECT 'Tetanus Toxoid (TT)', 'MOTHER', 4, 365,
       'Fourth dose of Tetanus Toxoid (at least 1 year after the 3rd dose, usually in a later pregnancy)',
       TRUE, FALSE, NULL, NOW()
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM VACCINE_SCHEDULE
                    WHERE target_group = 'MOTHER' AND vaccine_name = 'Tetanus Toxoid (TT)' AND dose_number = 4);

INSERT INTO VACCINE_SCHEDULE
    (vaccine_name, target_group, dose_number, recommended_age_days, description,
     is_active, is_conditional, target_gender, created_at)
SELECT 'Tetanus Toxoid (TT)', 'MOTHER', 5, 365,
       'Fifth dose of Tetanus Toxoid (at least 1 year after the 4th dose); protects for all childbearing years',
       TRUE, FALSE, NULL, NOW()
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM VACCINE_SCHEDULE
                    WHERE target_group = 'MOTHER' AND vaccine_name = 'Tetanus Toxoid (TT)' AND dose_number = 5);

-- ---------------------------------------------------------------------
-- 4. Verification queries (read-only)
-- ---------------------------------------------------------------------
-- SHOW COLUMNS FROM MOTHER LIKE 'prior_tt%';
-- SELECT schedule_id, vaccine_name, dose_number, is_active
--   FROM VACCINE_SCHEDULE WHERE target_group = 'MOTHER' ORDER BY vaccine_name, dose_number;
