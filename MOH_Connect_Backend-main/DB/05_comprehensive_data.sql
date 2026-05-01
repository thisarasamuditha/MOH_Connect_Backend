-- ====================================================
-- MOH Connect Backend - Comprehensive Sample Data
-- ====================================================
-- Populates all tables with 5 rows each
-- Generated: February 20, 2026
-- ====================================================

USE moh_db;

-- Clear existing data (in reverse order of dependencies)
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE NOTIFICATION;
TRUNCATE TABLE NOTIFICATION_TYPE;
TRUNCATE TABLE SESSION_ATTENDANCE;
TRUNCATE TABLE SESSION;
TRUNCATE TABLE SESSION_TYPE;
TRUNCATE TABLE TRIPOSHA_DISTRIBUTION;
TRUNCATE TABLE MEDICATION;
TRUNCATE TABLE BABY_VACCINATION;
TRUNCATE TABLE MOTHER_VACCINATION;
TRUNCATE TABLE VACCINE_SCHEDULE;
TRUNCATE TABLE BABY_RECORD;
TRUNCATE TABLE MOTHER_RECORD;
TRUNCATE TABLE BABY;
TRUNCATE TABLE PREGNANCY;
TRUNCATE TABLE MOTHER;
TRUNCATE TABLE DOCTOR;
TRUNCATE TABLE MIDWIFE;
TRUNCATE TABLE PHM_AREA;
TRUNCATE TABLE USER;

SET FOREIGN_KEY_CHECKS = 1;

-- ====================================================
-- 1. USER TABLE (5 rows - Admin, Midwife, Doctor, 2 Mothers)
-- ====================================================
-- Password for all users: 'password123' (bcrypt hash)
INSERT INTO USER (username, email, password_hash, role, is_active) VALUES
('admin_moh', 'admin@moh.gov.lk', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', TRUE),
('midwife_nimal', 'nimal.perera@moh.gov.lk', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MIDWIFE', TRUE),
('midwife_chamari', 'chamari.silva@moh.gov.lk', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MIDWIFE', TRUE),
('midwife_sachini', 'sachini.fernando@moh.gov.lk', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MIDWIFE', TRUE),
('dr_ruwan', 'dr.ruwan@moh.gov.lk', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'DOCTOR', TRUE),
('dr_samantha', 'dr.samantha@moh.gov.lk', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'DOCTOR', TRUE),
('mother_priya', 'priya.silva@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MOTHER', TRUE),
('mother_dilini', 'dilini.fernando@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MOTHER', TRUE),
('mother_sanduni', 'sanduni.perera@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MOTHER', TRUE),
('mother_madhavi', 'madhavi.rajapaksha@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MOTHER', TRUE),
('mother_nimali', 'nimali.wickramasinghe@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MOTHER', TRUE);

-- ====================================================
-- 2. PHM_AREA TABLE (5 rows)
-- ====================================================
INSERT INTO PHM_AREA (area_name, area_code) VALUES
('Colombo Central', 'PHM-COL-001'),
('Colombo North', 'PHM-COL-002'),
('Kandy City', 'PHM-KDY-001'),
('Galle Fort', 'PHM-GAL-001'),
('Kurunegala Central', 'PHM-KUR-001');

-- ====================================================
-- 3. MIDWIFE TABLE (3 rows)
-- ====================================================
INSERT INTO MIDWIFE (user_id, phm_area_id, name, contact_number, email, assignment_date, qualifications) VALUES
(2, 1, 'Nimal Perera', '0771234567', 'nimal.perera@moh.gov.lk', '2024-01-15', 'Diploma in Midwifery, BSc Nursing'),
(3, 2, 'Chamari Silva', '0771234568', 'chamari.silva@moh.gov.lk', '2023-06-20', 'Diploma in Midwifery, Certificate in Nutrition'),
(4, 3, 'Sachini Fernando', '0771234569', 'sachini.fernando@moh.gov.lk', '2023-03-10', 'Diploma in Midwifery');

-- ====================================================
-- 4. DOCTOR TABLE (2 rows)
-- ====================================================
INSERT INTO DOCTOR (user_id, name, specialization, contact_number, email, license_number) VALUES
(5, 'Dr. Ruwan Perera', 'Obstetrics and Gynecology', '0771234570', 'dr.ruwan@moh.gov.lk', 'SLMC-12345'),
(6, 'Dr. Samantha Fernando', 'Pediatrics', '0771234571', 'dr.samantha@moh.gov.lk', 'SLMC-12346');

-- ====================================================
-- 5. MOTHER TABLE (5 rows)
-- ====================================================
INSERT INTO MOTHER (user_id, phm_area_id, address, nic, name, date_of_birth, occupation, contact_number, blood_group, registration_date, is_active) VALUES
(7, 1, '123 Main Street, Colombo 03', '900234567V', 'Priya Silva', '1990-02-15', 'Teacher', '0771234572', 'A+', '2025-12-06', TRUE),
(8, 2, '456 Galle Road, Colombo 04', '920567890V', 'Dilini Fernando', '1992-05-20', 'Accountant', '0771234573', 'AB+', '2025-11-10', TRUE),
(9, 3, '789 Peradeniya Road, Kandy', '910789012V', 'Sanduni Perera', '1991-07-10', 'Nurse', '0771234574', 'O+', '2025-10-15', TRUE),
(10, 4, '234 Matara Road, Galle', '930901234V', 'Madhavi Rajapaksha', '1993-09-25', 'Designer', '0771234575', 'A+', '2025-09-20', TRUE),
(11, 5, '567 Ward Place, Kurunegala', '940223456V', 'Nimali Wickramasinghe', '1994-02-28', 'Pharmacist', '0771234576', 'B+', '2025-08-05', TRUE);

-- ====================================================
-- 6. PREGNANCY TABLE (5 rows)
-- ====================================================
INSERT INTO PREGNANCY (mother_id, pregnancy_number, lmp_date, edd_date, delivery_date, delivery_type, pregnancy_status, gravida, para, risk_level, risk_factors) VALUES
(1, 'PRG-2026-001', '2025-11-01', '2026-08-08', NULL, NULL, 'ACTIVE', 2, 1, 'LOW', NULL),
(2, 'PRG-2026-002', '2025-10-15', '2026-07-22', NULL, NULL, 'ACTIVE', 1, 0, 'MEDIUM', 'Family history of gestational diabetes'),
(3, 'PRG-2026-003', '2025-09-20', '2026-06-27', NULL, NULL, 'ACTIVE', 3, 2, 'LOW', NULL),
(4, 'PRG-2026-004', '2025-08-10', '2026-05-17', NULL, NULL, 'ACTIVE', 1, 0, 'HIGH', 'Advanced maternal age (>35), previous C-section'),
(5, 'PRG-2026-005', '2025-07-05', '2026-04-11', NULL, NULL, 'ACTIVE', 2, 1, 'MEDIUM', 'Mild hypertension');

-- ====================================================
-- 7. BABY TABLE (5 rows)
-- ====================================================
INSERT INTO BABY (pregnancy_id, mother_id, name, date_of_birth, gender, birth_weight, birth_height, birth_complications, apgar_score, birth_order, is_alive) VALUES
(1, 1, 'Baby Silva', '2025-08-05', 'FEMALE', 3.2, 49.5, NULL, '9/10', 1, TRUE),
(2, 2, 'Baby Fernando', '2025-07-20', 'MALE', 3.4, 50.8, NULL, '9/10', 1, TRUE),
(3, 3, 'Baby Perera', '2025-06-25', 'FEMALE', 3.1, 48.9, 'Mild jaundice', '8/9', 1, TRUE),
(4, 4, 'Baby Rajapaksha', '2025-05-15', 'MALE', 3.5, 51.2, NULL, '9/10', 1, TRUE),
(5, 5, 'Baby Wickramasinghe', '2025-04-10', 'FEMALE', 3.3, 50.1, NULL, '9/10', 1, TRUE);

-- ====================================================
-- 8. MOTHER_RECORD TABLE (5 rows)
-- ====================================================
INSERT INTO MOTHER_RECORD (pregnancy_id, midwife_id, doctor_id, record_date, gestational_age, weight, bmi, blood_pressure, shf, findings, recommendations, complications, notes, next_visit_date) VALUES
(1, 1, NULL, '2026-02-09', 14, 58.5, 22.3, '115/75', 14.0, 'Fetal heartbeat detected, normal development', 'Continue prenatal vitamins, adequate rest', NULL, 'Mother reports feeling well, no concerns', '2026-03-09'),
(2, 2, 1, '2026-01-20', 14, 62.0, 23.8, '120/78', 14.5, 'All parameters within normal range', 'Monitor blood sugar levels, balanced diet', NULL, 'Family history of diabetes noted', '2026-02-20'),
(3, 3, NULL, '2026-01-10', 16, 55.0, 21.5, '110/70', 16.0, 'Healthy pregnancy progression', 'Continue current routine', NULL, 'Third pregnancy, experienced mother', '2026-02-10'),
(4, 1, 1, '2025-12-15', 18, 68.5, 26.2, '130/85', 18.0, 'Elevated blood pressure, monitoring required', 'Bed rest advised, daily BP monitoring', 'Mild pregnancy-induced hypertension', 'High-risk pregnancy, frequent monitoring needed', '2026-01-05'),
(5, 2, NULL, '2025-11-20', 20, 60.0, 22.9, '125/80', 20.0, 'Slight elevation in BP, otherwise normal', 'Reduce salt intake, light exercise', 'Mild hypertension', 'Managing BP with lifestyle modifications', '2025-12-20');

-- ====================================================
-- 9. BABY_RECORD TABLE (5 rows)
-- ====================================================
INSERT INTO BABY_RECORD (baby_id, midwife_id, doctor_id, record_date, age_months, weight, height, head_circumference, bmi, developmental_milestones, growth_status, findings, recommendations, health_status, notes, next_visit_date) VALUES
(1, 1, 2, '2025-11-05', 3, 5.8, 60.5, 40.2, 15.8, 'Good head control, responds to sounds, social smiling', 'NORMAL', 'Healthy development, meeting all milestones', 'Continue breastfeeding, tummy time exercises', 'Excellent', 'Baby is active and responsive', '2025-12-05'),
(2, 2, 2, '2025-10-20', 3, 6.2, 61.0, 40.5, 16.7, 'Strong neck control, grasping objects, cooing', 'NORMAL', 'Weight gain appropriate for age', 'Exclusive breastfeeding continued', 'Very Good', 'No concerns noted', '2025-11-20'),
(3, 3, 2, '2025-09-25', 3, 5.5, 59.0, 39.8, 15.8, 'Responding to stimuli, tracking objects', 'NORMAL', 'Recovered from neonatal jaundice, developing well', 'Monitor skin color, continue feeding schedule', 'Good', 'Previous jaundice resolved completely', '2025-10-25'),
(4, 1, 2, '2025-08-15', 3, 6.5, 62.0, 41.0, 16.9, 'Rolling over, babbling, social interaction', 'NORMAL', 'Excellent physical and cognitive development', 'Ready to introduce solid foods next month', 'Excellent', 'Advanced for age', '2025-09-15'),
(5, 2, 2, '2025-07-10', 3, 5.9, 60.0, 40.0, 16.4, 'Good head control, responsive to voice', 'NORMAL', 'Normal development pattern', 'Continue current feeding routine', 'Good', 'Mother doing well with care', '2025-08-10');

-- ====================================================
-- 10. VACCINE_SCHEDULE TABLE (5 representative rows)
-- (Full schedule exists in 04_vaccination_data.sql)
-- ====================================================
INSERT INTO VACCINE_SCHEDULE (vaccine_name, target_group, dose_number, recommended_age_days, description) VALUES
('Tetanus Toxoid (TT)', 'MOTHER', 1, 0, 'First dose of Tetanus Toxoid during pregnancy'),
('Tetanus Toxoid (TT)', 'MOTHER', 2, 28, 'Second dose of Tetanus Toxoid during pregnancy'),
('BCG', 'BABY', 1, 0, 'BCG vaccine at birth'),
('DTP', 'BABY', 1, 60, 'First dose of DTP at 2 months'),
('Measles', 'BABY', 1, 270, 'Measles vaccine at 9 months');

-- ====================================================
-- 11. MOTHER_VACCINATION TABLE (5 rows)
-- ====================================================
INSERT INTO MOTHER_VACCINATION (pregnancy_id, midwife_id, schedule_id, vaccination_date, batch_number, manufacturer, next_dose_date, adverse_reaction) VALUES
(1, 1, 1, '2026-02-09', 'TT-2026-001', 'Serum Institute of India', '2026-03-09', NULL),
(2, 2, 1, '2026-01-20', 'TT-2026-002', 'Serum Institute of India', '2026-02-17', NULL),
(3, 3, 1, '2026-01-10', 'TT-2026-003', 'Serum Institute of India', '2026-02-07', NULL),
(4, 1, 1, '2025-12-15', 'TT-2025-089', 'Serum Institute of India', '2026-01-12', NULL),
(5, 2, 1, '2025-11-20', 'TT-2025-078', 'Serum Institute of India', '2025-12-18', 'Mild soreness at injection site');

-- ====================================================
-- 12. BABY_VACCINATION TABLE (5 rows)
-- ====================================================
INSERT INTO BABY_VACCINATION (baby_id, midwife_id, schedule_id, vaccination_date, batch_number, manufacturer, next_dose_date, adverse_reaction) VALUES
(1, 1, 3, '2025-08-05', 'BCG-2025-150', 'Japan BCG Laboratory', NULL, NULL),
(2, 2, 3, '2025-07-20', 'BCG-2025-148', 'Japan BCG Laboratory', NULL, NULL),
(3, 3, 3, '2025-06-25', 'BCG-2025-142', 'Japan BCG Laboratory', NULL, 'Small pustule at injection site (normal reaction)'),
(4, 1, 3, '2025-05-15', 'BCG-2025-135', 'Japan BCG Laboratory', NULL, NULL),
(5, 2, 3, '2025-04-10', 'BCG-2025-128', 'Japan BCG Laboratory', NULL, NULL);

-- ====================================================
-- 13. MEDICATION TABLE (5 rows)
-- ====================================================
INSERT INTO MEDICATION (pregnancy_id, midwife_id, doctor_id, medication_name, dosage, start_date, end_date, frequency, route, indication, notes) VALUES
(1, 1, NULL, 'Folic Acid', '5mg', '2025-11-01', '2026-08-08', 'Once daily', 'ORAL', 'Neural tube defect prevention', 'Continue throughout pregnancy'),
(2, 2, 1, 'Folic Acid', '5mg', '2025-10-15', '2026-07-22', 'Once daily', 'ORAL', 'Neural tube defect prevention', 'Standard prenatal care'),
(2, 2, 1, 'Metformin', '500mg', '2026-01-20', NULL, 'Twice daily', 'ORAL', 'Blood sugar control', 'Monitor glucose levels regularly'),
(4, 1, 1, 'Methyldopa', '250mg', '2025-12-15', NULL, 'Twice daily', 'ORAL', 'Hypertension management', 'Monitor BP daily, report if >140/90'),
(5, 2, NULL, 'Ferrous Sulfate', '200mg', '2025-11-20', '2026-04-11', 'Once daily', 'ORAL', 'Anemia prevention', 'Take with vitamin C for better absorption');

-- ====================================================
-- 14. TRIPOSHA_DISTRIBUTION TABLE (5 rows)
-- ====================================================
INSERT INTO TRIPOSHA_DISTRIBUTION (baby_id, midwife_id, distribution_date, quantity, packet_type, next_distribution_date, beneficiary_signature, notes) VALUES
(1, 1, '2025-11-05', 2.0, 'STANDARD', '2025-12-05', NULL, 'First distribution at 3 months'),
(2, 2, '2025-10-20', 2.0, 'STANDARD', '2025-11-20', NULL, 'Regular distribution schedule'),
(3, 3, '2025-09-25', 2.0, 'STANDARD', '2025-10-25', NULL, 'Mother satisfied with supplement'),
(4, 1, '2025-08-15', 2.0, 'STANDARD', '2025-09-15', NULL, 'Baby gaining weight well'),
(5, 2, '2025-07-10', 2.0, 'SPECIAL', '2025-08-10', NULL, 'Special formula due to mild lactose sensitivity');

-- ====================================================
-- 15. SESSION_TYPE TABLE (5 rows)
-- ====================================================
INSERT INTO SESSION_TYPE (type_name, description, target_audience) VALUES
('Prenatal Care Workshop', 'Comprehensive education on pregnancy care, nutrition, and preparation for childbirth', 'PREGNANT_MOTHERS'),
('Breastfeeding Support', 'Guidance and support on breastfeeding techniques, common challenges, and solutions', 'NEW_MOTHERS'),
('Child Development', 'Information on developmental milestones, growth monitoring, and early childhood care', 'FAMILIES'),
('Nutrition Education', 'Nutritional guidance for mothers during pregnancy and breastfeeding, infant nutrition', 'ALL'),
('Vaccination Awareness', 'Education on immunization schedules, vaccine importance, and safety', 'ALL');

-- ====================================================
-- 16. SESSION TABLE (5 rows)
-- ====================================================
INSERT INTO SESSION (midwife_id, session_type_id, phm_area_id, session_date, start_time, end_time, topic, venue, description, capacity, status) VALUES
(1, 1, 1, '2026-03-15', '09:00:00', '12:00:00', 'Preparing for Your Baby', 'Colombo Central Community Hall', 'Comprehensive session on labor, delivery, and newborn care basics', 30, 'SCHEDULED'),
(2, 2, 2, '2026-02-25', '14:00:00', '16:00:00', 'Breastfeeding Success', 'Colombo North Clinic', 'Hands-on breastfeeding workshop with practical demonstrations', 20, 'SCHEDULED'),
(3, 3, 3, '2026-02-20', '10:00:00', '12:00:00', 'Your Baby\'s First Year', 'Kandy City Medical Center', 'Understanding developmental milestones and growth expectations', 25, 'COMPLETED'),
(1, 4, 1, '2026-01-30', '09:00:00', '11:00:00', 'Healthy Eating for Two', 'Colombo Central Community Hall', 'Nutritional requirements during pregnancy and meal planning', 35, 'COMPLETED'),
(2, 5, 2, '2026-01-15', '14:00:00', '16:00:00', 'Protect Your Child', 'Colombo North Clinic', 'Complete vaccination schedule and importance of immunization', 40, 'COMPLETED');

-- ====================================================
-- 17. SESSION_ATTENDANCE TABLE (5 rows)
-- ====================================================
INSERT INTO SESSION_ATTENDANCE (session_id, mother_id, attended, attendance_time, notes, feedback) VALUES
(3, 3, TRUE, '2026-02-20 10:05:00', 'Active participation, asked several questions', 'Very informative session, learned a lot'),
(4, 1, TRUE, '2026-01-30 09:00:00', 'Punctual attendance, took detailed notes', 'Excellent presentation, practical tips were helpful'),
(4, 2, TRUE, '2026-01-30 09:10:00', 'Brought food diary for review', 'Great session, nutritionist was very knowledgeable'),
(5, 1, TRUE, '2026-01-15 14:00:00', 'Concerned about vaccine safety', 'Addressed all my concerns, feeling confident now'),
(5, 4, TRUE, '2026-01-15 14:15:00', 'Took vaccination schedule card', 'Clear explanation of EPI schedule');

-- ====================================================
-- 18. NOTIFICATION_TYPE TABLE (5 rows)
-- ====================================================
INSERT INTO NOTIFICATION_TYPE (type_name, description, template, priority) VALUES
('Appointment Reminder', 'Reminder for upcoming medical appointments', 'Dear {mother_name}, reminder for your appointment on {date} at {time}.', 'MEDIUM'),
('Vaccination Due', 'Notification for upcoming vaccinations', 'Your child {baby_name} is due for {vaccine_name} on {date}.', 'HIGH'),
('Session Invitation', 'Invitation to health education sessions', 'You are invited to {session_topic} on {date} at {venue}.', 'MEDIUM'),
('Health Alert', 'Urgent health-related notifications', 'IMPORTANT: {message}. Contact your healthcare provider immediately.', 'URGENT'),
('Triposha Collection', 'Notification for nutrition supplement collection', 'Triposha distribution for {baby_name} is available on {date} at {location}.', 'LOW');

-- ====================================================
-- 19. NOTIFICATION TABLE (5 rows)
-- ====================================================
INSERT INTO NOTIFICATION (type_id, midwife_id, mother_id, sent_date, message, status, delivery_method, event_date, event_type, read_at, responded_at) VALUES
(1, 1, 1, '2026-03-07 09:00:00', 'Dear Priya Silva, reminder for your appointment on 2026-03-09 at 09:00 AM.', 'DELIVERED', 'SMS', '2026-03-09', 'APPOINTMENT', '2026-03-07 10:30:00', NULL),
(2, 2, 2, '2026-02-15 10:00:00', 'Your child Baby Fernando is due for DTP vaccination on 2026-02-20.', 'DELIVERED', 'WHATSAPP', '2026-02-20', 'VACCINATION', '2026-02-15 11:20:00', '2026-02-15 14:30:00'),
(3, 1, 1, '2026-03-05 08:00:00', 'You are invited to Preparing for Your Baby on 2026-03-15 at Colombo Central Community Hall.', 'SENT', 'EMAIL', '2026-03-15', 'SESSION', NULL, NULL),
(4, 1, 4, '2026-01-05 14:00:00', 'IMPORTANT: Your blood pressure reading was elevated. Please monitor daily and contact if >140/90.', 'DELIVERED', 'CALL', '2026-01-05', 'HEALTH_ALERT', '2026-01-05 14:15:00', '2026-01-05 15:00:00'),
(5, 1, 1, '2025-11-28 09:00:00', 'Triposha distribution for Baby Silva is available on 2025-12-05 at Colombo Central Clinic.', 'DELIVERED', 'SMS', '2025-12-05', 'TRIPOSHA', '2025-11-28 16:45:00', NULL);

-- ====================================================
-- DATA VERIFICATION QUERIES
-- ====================================================

-- Verify record counts
SELECT 'USER' AS TableName, COUNT(*) AS RecordCount FROM USER
UNION ALL SELECT 'PHM_AREA', COUNT(*) FROM PHM_AREA
UNION ALL SELECT 'MIDWIFE', COUNT(*) FROM MIDWIFE
UNION ALL SELECT 'DOCTOR', COUNT(*) FROM DOCTOR
UNION ALL SELECT 'MOTHER', COUNT(*) FROM MOTHER
UNION ALL SELECT 'PREGNANCY', COUNT(*) FROM PREGNANCY
UNION ALL SELECT 'BABY', COUNT(*) FROM BABY
UNION ALL SELECT 'MOTHER_RECORD', COUNT(*) FROM MOTHER_RECORD
UNION ALL SELECT 'BABY_RECORD', COUNT(*) FROM BABY_RECORD
UNION ALL SELECT 'VACCINE_SCHEDULE', COUNT(*) FROM VACCINE_SCHEDULE
UNION ALL SELECT 'MOTHER_VACCINATION', COUNT(*) FROM MOTHER_VACCINATION
UNION ALL SELECT 'BABY_VACCINATION', COUNT(*) FROM BABY_VACCINATION
UNION ALL SELECT 'MEDICATION', COUNT(*) FROM MEDICATION
UNION ALL SELECT 'TRIPOSHA_DISTRIBUTION', COUNT(*) FROM TRIPOSHA_DISTRIBUTION
UNION ALL SELECT 'SESSION_TYPE', COUNT(*) FROM SESSION_TYPE
UNION ALL SELECT 'SESSION', COUNT(*) FROM SESSION
UNION ALL SELECT 'SESSION_ATTENDANCE', COUNT(*) FROM SESSION_ATTENDANCE
UNION ALL SELECT 'NOTIFICATION_TYPE', COUNT(*) FROM NOTIFICATION_TYPE
UNION ALL SELECT 'NOTIFICATION', COUNT(*) FROM NOTIFICATION;

-- ====================================================
-- END OF COMPREHENSIVE DATA POPULATION
-- ====================================================
