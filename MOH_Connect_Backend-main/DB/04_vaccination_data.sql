-- ================================================================
-- VACCINATION SYSTEM SAMPLE DATA
-- ================================================================

-- Sample Vaccine Schedules for Mothers
INSERT INTO VACCINE_SCHEDULE (vaccine_name, target_group, dose_number, recommended_age_days, description) VALUES
('Tetanus Toxoid (TT)', 'MOTHER', 1, 0, 'First dose of Tetanus Toxoid during pregnancy, ideally within the first trimester'),
('Tetanus Toxoid (TT)', 'MOTHER', 2, 28, 'Second dose of TT, 4 weeks after the first dose'),
('Tetanus Toxoid (TT)', 'MOTHER', 3, 180, 'Third dose of TT, 6 months after the second dose'),
('Influenza Vaccine', 'MOTHER', 1, 0, 'Flu vaccine recommended during any trimester of pregnancy'),
('COVID-19 Vaccine', 'MOTHER', 1, 0, 'First dose of COVID-19 vaccine during pregnancy'),
('COVID-19 Vaccine', 'MOTHER', 2, 21, 'Second dose of COVID-19 vaccine, 3 weeks after first dose');

-- Sample Vaccine Schedules for Babies (Sri Lanka Expanded Programme of Immunization - EPI)
INSERT INTO VACCINE_SCHEDULE (vaccine_name, target_group, dose_number, recommended_age_days, description) VALUES
('BCG', 'BABY', 1, 0, 'Bacillus Calmette-Guérin vaccine at birth for tuberculosis protection'),
('Hepatitis B', 'BABY', 1, 0, 'First dose of Hepatitis B vaccine at birth'),
('OPV (Oral Polio Vaccine)', 'BABY', 1, 0, 'Oral Polio Vaccine at birth'),

-- 2 months
('DTP', 'BABY', 1, 60, 'First dose of Diphtheria, Tetanus, Pertussis vaccine at 2 months'),
('Hib', 'BABY', 1, 60, 'First dose of Haemophilus influenzae type b vaccine at 2 months'),
('Hepatitis B', 'BABY', 2, 60, 'Second dose of Hepatitis B vaccine at 2 months'),
('OPV', 'BABY', 2, 60, 'Second dose of Oral Polio Vaccine at 2 months'),

-- 4 months
('DTP', 'BABY', 2, 120, 'Second dose of DTP vaccine at 4 months'),
('Hib', 'BABY', 2, 120, 'Second dose of Hib vaccine at 4 months'),
('OPV', 'BABY', 3, 120, 'Third dose of Oral Polio Vaccine at 4 months'),

-- 6 months
('DTP', 'BABY', 3, 180, 'Third dose of DTP vaccine at 6 months'),
('Hib', 'BABY', 3, 180, 'Third dose of Hib vaccine at 6 months'),
('Hepatitis B', 'BABY', 3, 180, 'Third dose of Hepatitis B vaccine at 6 months'),
('OPV', 'BABY', 4, 180, 'Fourth dose of Oral Polio Vaccine at 6 months'),

-- 9 months
('Measles', 'BABY', 1, 270, 'First dose of Measles vaccine at 9 months'),

-- 12 months
('MMR', 'BABY', 1, 365, 'Measles, Mumps, Rubella vaccine at 12 months'),

-- 18 months
('DTP', 'BABY', 4, 540, 'Fourth dose (booster) of DTP vaccine at 18 months'),
('OPV', 'BABY', 5, 540, 'Fifth dose (booster) of Oral Polio Vaccine at 18 months'),

-- 3 years
('JE (Japanese Encephalitis)', 'BABY', 1, 1095, 'First dose of Japanese Encephalitis vaccine at 3 years'),

-- 5 years (before school entry)
('DT (Diphtheria-Tetanus)', 'BABY', 1, 1825, 'Booster dose of Diphtheria-Tetanus vaccine at 5 years'),
('MMR', 'BABY', 2, 1825, 'Second dose (booster) of MMR vaccine at 5 years');

-- Note: The above schedules follow Sri Lanka's National Immunization Programme (EPI)
-- Recommended age in days is approximate and may vary based on individual circumstances
