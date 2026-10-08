-- =====================================================================
-- 07_clinical_workflows.sql
-- Schema + reference-data changes for the clinical workflow release:
--   * pregnancy outcome (delivery / miscarriage / termination)
--   * haemoglobin on antenatal (ANC) visit records
--   * vaccine schedule aligned to the current Sri Lanka EPI
--
-- RUN THIS BEFORE DEPLOYING the backend build that contains these features.
-- The backend maps the new columns, so every query on PREGNANCY,
-- MOTHER_RECORD and VACCINE_SCHEDULE fails until the columns exist.
--
-- Safe to run more than once (IF NOT EXISTS / NOT EXISTS guards).
-- Tested syntax: TiDB / MySQL 8 / MariaDB 10.x.
-- Take a backup first.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. PREGNANCY outcome fields
-- ---------------------------------------------------------------------
ALTER TABLE PREGNANCY ADD COLUMN IF NOT EXISTS outcome_date DATE NULL;
ALTER TABLE PREGNANCY ADD COLUMN IF NOT EXISTS outcome_notes TEXT NULL;
ALTER TABLE PREGNANCY ADD COLUMN IF NOT EXISTS delivery_place VARCHAR(255) NULL;

-- Backfill outcome_date for pregnancies already closed with a delivery date
UPDATE PREGNANCY
   SET outcome_date = delivery_date
 WHERE outcome_date IS NULL
   AND delivery_date IS NOT NULL
   AND pregnancy_status <> 'ACTIVE';

-- ---------------------------------------------------------------------
-- 2. MOTHER_RECORD: haemoglobin (g/dL)
-- ---------------------------------------------------------------------
ALTER TABLE MOTHER_RECORD ADD COLUMN IF NOT EXISTS hemoglobin FLOAT NULL;

-- ---------------------------------------------------------------------
-- 3. VACCINE_SCHEDULE: active flag, conditional doses, gender-specific doses
-- ---------------------------------------------------------------------
ALTER TABLE VACCINE_SCHEDULE ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE VACCINE_SCHEDULE ADD COLUMN IF NOT EXISTS is_conditional BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE VACCINE_SCHEDULE ADD COLUMN IF NOT EXISTS target_gender VARCHAR(10) NULL;  -- NULL = all, 'FEMALE', 'MALE'

-- ---------------------------------------------------------------------
-- 4. Current Sri Lanka National Immunization Schedule (children)
--    Insert each row only if an identical (name, dose, age) row does not exist.
--    Ages are in days from date of birth.
--    >>> To be verified and signed off by the MOH / Medical Officer. <<<
-- ---------------------------------------------------------------------
CREATE TEMPORARY TABLE tmp_epi (
    vaccine_name VARCHAR(255),
    dose_number INT,
    recommended_age_days INT,
    is_conditional BOOLEAN,
    target_gender VARCHAR(10),
    description TEXT
);

INSERT INTO tmp_epi VALUES
('BCG',                         1,    0, FALSE, NULL,     'At birth'),
('Pentavalent (DTP-HepB-Hib)',  1,   60, FALSE, NULL,     '2 months completed - 1st dose'),
('OPV',                         1,   60, FALSE, NULL,     '2 months completed - Oral Polio Vaccine 1st dose'),
('fIPV',                        1,   60, FALSE, NULL,     '2 months completed - fractional Inactivated Polio Vaccine 1st dose'),
('Pentavalent (DTP-HepB-Hib)',  2,  120, FALSE, NULL,     '4 months completed - 2nd dose'),
('OPV',                         2,  120, FALSE, NULL,     '4 months completed - Oral Polio Vaccine 2nd dose'),
('fIPV',                        2,  120, FALSE, NULL,     '4 months completed - fractional Inactivated Polio Vaccine 2nd dose'),
('Pentavalent (DTP-HepB-Hib)',  3,  180, FALSE, NULL,     '6 months completed - 3rd dose'),
('OPV',                         3,  180, FALSE, NULL,     '6 months completed - Oral Polio Vaccine 3rd dose'),
('BCG',                         2,  180, TRUE,  NULL,     '6 months completed - 2nd dose ONLY if no BCG scar'),
('MMR',                         1,  270, FALSE, NULL,     '9 months completed - Measles, Mumps, Rubella 1st dose'),
('Live JE',                     1,  365, FALSE, NULL,     '12 months completed - Live Japanese Encephalitis vaccine'),
('DPT',                         4,  540, FALSE, NULL,     '18 months completed - DPT booster'),
('OPV',                         4,  540, FALSE, NULL,     '18 months completed - Oral Polio Vaccine 4th dose'),
('MMR',                         2, 1095, FALSE, NULL,     '3 years completed - Measles, Mumps, Rubella 2nd dose'),
('DT',                          5, 1825, FALSE, NULL,     '5 years completed - Diphtheria & Tetanus booster'),
('OPV',                         5, 1825, FALSE, NULL,     '5 years completed - Oral Polio Vaccine 5th dose'),
('HPV',                         1, 3650, FALSE, 'FEMALE', '10 years completed (girls) - Human Papillomavirus 1st dose'),
('HPV',                         2, 3833, FALSE, 'FEMALE', '10 years + 6 months (girls) - Human Papillomavirus 2nd dose'),
('aTd',                         1, 4015, FALSE, NULL,     '11 years completed - adult Tetanus-diphtheria booster');

INSERT INTO VACCINE_SCHEDULE
    (vaccine_name, target_group, dose_number, recommended_age_days, description,
     is_active, is_conditional, target_gender, created_at)
SELECT t.vaccine_name, 'BABY', t.dose_number, t.recommended_age_days, t.description,
       TRUE, t.is_conditional, t.target_gender, NOW()
  FROM tmp_epi t
 WHERE NOT EXISTS (
        SELECT 1 FROM VACCINE_SCHEDULE v
         WHERE v.target_group = 'BABY'
           AND v.vaccine_name = t.vaccine_name
           AND v.dose_number = t.dose_number
           AND v.recommended_age_days = t.recommended_age_days);

-- Make sure the matching rows carry the current flags/description
UPDATE VACCINE_SCHEDULE v
  JOIN tmp_epi t
    ON v.target_group = 'BABY'
   AND v.vaccine_name = t.vaccine_name
   AND v.dose_number = t.dose_number
   AND v.recommended_age_days = t.recommended_age_days
   SET v.is_active = TRUE,
       v.is_conditional = t.is_conditional,
       v.target_gender = t.target_gender,
       v.description = t.description;

-- Deactivate outdated child schedule rows.
-- They are NOT deleted: existing BABY_VACCINATION rows still reference them.
UPDATE VACCINE_SCHEDULE v
   SET v.is_active = FALSE
 WHERE v.target_group = 'BABY'
   AND NOT EXISTS (
        SELECT 1 FROM tmp_epi t
         WHERE t.vaccine_name = v.vaccine_name
           AND t.dose_number = v.dose_number
           AND t.recommended_age_days = v.recommended_age_days);

DROP TEMPORARY TABLE tmp_epi;

-- ---------------------------------------------------------------------
-- 5. Verification queries (read-only)
-- ---------------------------------------------------------------------
-- SELECT schedule_id, vaccine_name, dose_number, recommended_age_days, is_active, is_conditional, target_gender
--   FROM VACCINE_SCHEDULE WHERE target_group = 'BABY' ORDER BY is_active DESC, recommended_age_days, vaccine_name;
-- SHOW COLUMNS FROM PREGNANCY;
-- SHOW COLUMNS FROM MOTHER_RECORD;
