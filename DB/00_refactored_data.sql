-- ====================================================
-- MOH Connect Backend - Sample Data for Refactored Schema
-- ====================================================
-- Matches Java Entity Models exactly
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
TRUNCATE TABLE BABY_TRIPOSHA_DISTRIBUTION;
TRUNCATE TABLE MOTHER_TRIPOSHA_DISTRIBUTION;
TRUNCATE TABLE TRIPOSHA_STOCK;
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
-- 1. USER TABLE
-- Password for all users: 'password123' (bcrypt hash)
-- ====================================================
INSERT INTO USER (username, email, password_hash, role, is_active, created_at) VALUES
('admin_moh', 'admin@moh.gov.lk', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', TRUE, NOW()),
('midwife_nimal', 'nimal.perera@moh.gov.lk', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MIDWIFE', TRUE, NOW()),
('midwife_chamari', 'chamari.silva@moh.gov.lk', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MIDWIFE', TRUE, NOW()),
('midwife_sachini', 'sachini.fernando@moh.gov.lk', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MIDWIFE', TRUE, NOW()),
('dr_ruwan', 'dr.ruwan@moh.gov.lk', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'DOCTOR', TRUE, NOW()),
('dr_samantha', 'dr.samantha@moh.gov.lk', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'DOCTOR', TRUE, NOW()),
('mother_priya', 'priya.silva@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MOTHER', TRUE, NOW()),
('mother_dilini', 'dilini.fernando@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MOTHER', TRUE, NOW()),
('mother_sanduni', 'sanduni.perera@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MOTHER', TRUE, NOW()),
('mother_madhavi', 'madhavi.rajapaksha@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MOTHER', TRUE, NOW()),
('mother_nimali', 'nimali.wickramasinghe@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MOTHER', TRUE, NOW());

-- ====================================================
-- 2. PHM_AREA TABLE
-- ====================================================
INSERT INTO PHM_AREA (area_name, area_code) VALUES
('Colombo Central', 'PHM-COL-001'),
('Colombo North', 'PHM-COL-002'),
('Kandy City', 'PHM-KDY-001'),
('Galle Fort', 'PHM-GAL-001'),
('Kurunegala Central', 'PHM-KUR-001');

-- ====================================================
-- 3. MIDWIFE TABLE
-- ====================================================
INSERT INTO MIDWIFE (user_id, phm_area_id, name, contact_number, email, assignment_date, qualifications, created_at) VALUES
(2, 1, 'Nimal Perera', '0771234567', 'nimal.perera@moh.gov.lk', '2024-01-15', 'Diploma in Midwifery, BSc Nursing', NOW()),
(3, 2, 'Chamari Silva', '0771234568', 'chamari.silva@moh.gov.lk', '2023-06-20', 'Diploma in Midwifery, Certificate in Nutrition', NOW()),
(4, 3, 'Sachini Fernando', '0771234569', 'sachini.fernando@moh.gov.lk', '2023-03-10', 'Diploma in Midwifery', NOW());

-- ====================================================
-- 4. DOCTOR TABLE
-- ====================================================
INSERT INTO DOCTOR (user_id, name, specialization, contact_number, email, license_number, created_at) VALUES
(5, 'Dr. Ruwan Perera', 'Obstetrics and Gynecology', '0771234570', 'dr.ruwan@moh.gov.lk', 'SLMC-12345', NOW()),
(6, 'Dr. Samantha Fernando', 'Pediatrics', '0771234571', 'dr.samantha@moh.gov.lk', 'SLMC-12346', NOW());

-- ====================================================
-- 5. MOTHER TABLE
-- ====================================================
INSERT INTO MOTHER (user_id, phm_area_id, address, nic, name, date_of_birth, occupation, contact_number, blood_group, registration_date, is_active, created_at) VALUES
(7, 1, '123 Main Street, Colombo 03', '900234567V', 'Priya Silva', '1990-02-15', 'Teacher', '0771234572', 'A+', '2025-12-06', TRUE, NOW()),
(8, 2, '456 Galle Road, Colombo 04', '920567890V', 'Dilini Fernando', '1992-05-20', 'Accountant', '0771234573', 'AB+', '2025-11-10', TRUE, NOW()),
(9, 3, '789 Peradeniya Road, Kandy', '910789012V', 'Sanduni Perera', '1991-07-10', 'Nurse', '0771234574', 'O+', '2025-10-15', TRUE, NOW()),
(10, 4, '234 Matara Road, Galle', '930901234V', 'Madhavi Rajapaksha', '1993-09-25', 'Designer', '0771234575', 'A+', '2025-09-20', TRUE, NOW()),
(11, 5, '567 Ward Place, Kurunegala', '940223456V', 'Nimali Wickramasinghe', '1994-02-28', 'Pharmacist', '0771234576', 'B+', '2025-08-05', TRUE, NOW());

-- ====================================================
-- 6. PREGNANCY TABLE
-- ====================================================
INSERT INTO PREGNANCY (mother_id, pregnancy_number, lmp_date, edd_date, delivery_date, delivery_type, pregnancy_status, gravida, para, risk_level, risk_factors, created_at, updated_at) VALUES
(1, 'PRG-2026-001', '2025-11-01', '2026-08-08', NULL, NULL, 'ACTIVE', 2, 1, 'LOW', NULL, NOW(), NOW()),
(2, 'PRG-2026-002', '2025-10-15', '2026-07-22', NULL, NULL, 'ACTIVE', 1, 0, 'MEDIUM', 'Family history of gestational diabetes', NOW(), NOW()),
(3, 'PRG-2026-003', '2025-09-20', '2026-06-27', NULL, NULL, 'ACTIVE', 3, 2, 'LOW', NULL, NOW(), NOW()),
(4, 'PRG-2026-004', '2025-08-10', '2026-05-17', NULL, NULL, 'ACTIVE', 1, 0, 'HIGH', 'Advanced maternal age (>35), previous C-section', NOW(), NOW()),
(5, 'PRG-2026-005', '2025-07-05', '2026-04-11', NULL, NULL, 'ACTIVE', 2, 1, 'MEDIUM', 'Mild hypertension', NOW(), NOW());

-- ====================================================
-- 7. BABY TABLE
-- ====================================================
INSERT INTO BABY (pregnancy_id, mother_id, name, date_of_birth, gender, birth_weight, birth_height, birth_complications, apgar_score, birth_order, is_alive, created_at, updated_at) VALUES
(1, 1, 'Baby Silva', '2025-08-05', 'FEMALE', 3.2, 49.5, NULL, '9/10', 1, TRUE, NOW(), NOW()),
(2, 2, 'Baby Fernando', '2025-07-20', 'MALE', 3.4, 50.8, NULL, '9/10', 1, TRUE, NOW(), NOW()),
(3, 3, 'Baby Perera', '2025-06-25', 'FEMALE', 3.1, 48.9, 'Mild jaundice', '8/9', 1, TRUE, NOW(), NOW()),
(4, 4, 'Baby Rajapaksha', '2025-05-15', 'MALE', 3.5, 51.2, NULL, '9/10', 1, TRUE, NOW(), NOW()),
(5, 5, 'Baby Wickramasinghe', '2025-04-10', 'FEMALE', 3.3, 50.1, NULL, '9/10', 1, TRUE, NOW(), NOW());

-- ====================================================
-- 8. MOTHER_RECORD TABLE
-- ====================================================
INSERT INTO MOTHER_RECORD (pregnancy_id, midwife_id, doctor_id, record_date, gestational_age, weight, bmi, blood_pressure, shf, findings, recommendations, complications, notes, next_visit_date, created_at) VALUES
(1, 1, NULL, '2026-02-09', 14, 58.5, 22.3, '115/75', 14.0, 'Fetal heartbeat detected, normal development', 'Continue prenatal vitamins, adequate rest', NULL, 'Mother reports feeling well, no concerns', '2026-03-09', NOW()),
(2, 2, 1, '2026-01-20', 14, 62.0, 23.8, '120/78', 14.5, 'All parameters within normal range', 'Monitor blood sugar levels, balanced diet', NULL, 'Family history of diabetes noted', '2026-02-20', NOW()),
(3, 3, NULL, '2026-01-10', 16, 55.0, 21.5, '110/70', 16.0, 'Healthy pregnancy progression', 'Continue current routine', NULL, 'Third pregnancy, experienced mother', '2026-02-10', NOW()),
(4, 1, 1, '2025-12-15', 18, 68.5, 26.2, '130/85', 18.0, 'Elevated blood pressure, monitoring required', 'Bed rest advised, daily BP monitoring', 'Mild pregnancy-induced hypertension', 'High-risk pregnancy, frequent monitoring needed', '2026-01-05', NOW()),
(5, 2, NULL, '2025-11-20', 20, 60.0, 22.9, '125/80', 20.0, 'Slight elevation in BP, otherwise normal', 'Reduce salt intake, light exercise', 'Mild hypertension', 'Managing BP with lifestyle modifications', '2025-12-20', NOW());

-- ====================================================
-- 9. BABY_RECORD TABLE
-- ====================================================
INSERT INTO BABY_RECORD (baby_id, midwife_id, doctor_id, record_date, age_months, weight, height, head_circumference, bmi, developmental_milestones, growth_status, findings, recommendations, health_status, notes, next_visit_date, created_at) VALUES
(1, 1, 2, '2025-11-05', 3, 5.8, 60.5, 40.2, 15.8, 'Good head control, responds to sounds, social smiling', 'NORMAL', 'Healthy development, meeting all milestones', 'Continue breastfeeding, tummy time exercises', 'Excellent', 'Baby is active and responsive', '2025-12-05', NOW()),
(2, 2, 2, '2025-10-20', 3, 6.2, 61.0, 40.5, 16.7, 'Strong neck control, grasping objects, cooing', 'NORMAL', 'Weight gain appropriate for age', 'Exclusive breastfeeding continued', 'Very Good', 'No concerns noted', '2025-11-20', NOW()),
(3, 3, 2, '2025-09-25', 3, 5.5, 59.0, 39.8, 15.8, 'Responding to stimuli, tracking objects', 'NORMAL', 'Recovered from neonatal jaundice, developing well', 'Monitor skin color, continue feeding schedule', 'Good', 'Previous jaundice resolved completely', '2025-10-25', NOW()),
(4, 1, 2, '2025-08-15', 3, 6.5, 62.0, 41.0, 16.9, 'Rolling over, babbling, social interaction', 'NORMAL', 'Excellent physical and cognitive development', 'Ready to introduce solid foods next month', 'Excellent', 'Advanced for age', '2025-09-15', NOW()),
(5, 2, 2, '2025-07-10', 3, 5.9, 60.0, 40.0, 16.4, 'Good head control, responsive to voice', 'NORMAL', 'Normal development pattern', 'Continue current feeding routine', 'Good', 'Mother doing well with care', '2025-08-10', NOW());

-- ====================================================
-- 10. VACCINE_SCHEDULE TABLE
-- ====================================================
INSERT INTO VACCINE_SCHEDULE (vaccine_name, target_group, dose_number, recommended_age_days, description, created_at) VALUES
('Tetanus Toxoid (TT)', 'MOTHER', 1, 0, 'First dose of Tetanus Toxoid during pregnancy', NOW()),
('Tetanus Toxoid (TT)', 'MOTHER', 2, 28, 'Second dose of Tetanus Toxoid during pregnancy', NOW()),
('Tetanus Toxoid (TT)', 'MOTHER', 3, 56, 'Third dose of Tetanus Toxoid during pregnancy', NOW()),
('Influenza', 'MOTHER', 1, 90, 'Seasonal flu vaccine during pregnancy', NOW()),
('COVID-19', 'MOTHER', 1, 100, 'First dose of COVID-19 vaccine during pregnancy', NOW()),
('BCG', 'BABY', 1, 0, 'BCG vaccine at birth', NOW()),
('Hepatitis B', 'BABY', 1, 0, 'First dose of Hepatitis B at birth', NOW()),
('OPV', 'BABY', 1, 0, 'Oral Polio Vaccine at birth', NOW()),
('DTP', 'BABY', 1, 60, 'First dose of DTP at 2 months', NOW()),
('Hib', 'BABY', 1, 60, 'First dose of Hib at 2 months', NOW()),
('Hepatitis B', 'BABY', 2, 60, 'Second dose of Hepatitis B at 2 months', NOW()),
('OPV', 'BABY', 2, 60, 'Second dose of OPV at 2 months', NOW()),
('DTP', 'BABY', 2, 120, 'Second dose of DTP at 4 months', NOW()),
('Hib', 'BABY', 2, 120, 'Second dose of Hib at 4 months', NOW()),
('OPV', 'BABY', 3, 120, 'Third dose of OPV at 4 months', NOW()),
('Measles', 'BABY', 1, 270, 'Measles vaccine at 9 months', NOW()),
('MMR', 'BABY', 1, 365, 'MMR vaccine at 12 months', NOW()),
('Japanese Encephalitis', 'BABY', 1, 1095, 'JE vaccine at 3 years', NOW()),
('DT', 'BABY', 1, 1825, 'DT booster at 5 years', NOW()),
('MMR', 'BABY', 2, 1825, 'MMR booster at 5 years', NOW());

-- ====================================================
-- 11. MOTHER_VACCINATION TABLE
-- ====================================================
INSERT INTO MOTHER_VACCINATION (pregnancy_id, midwife_id, schedule_id, vaccination_date, batch_number, manufacturer, next_dose_date, adverse_reaction, created_at) VALUES
(1, 1, 1, '2026-02-09', 'TT-2026-001', 'Serum Institute of India', '2026-03-09', NULL, NOW()),
(2, 2, 1, '2026-01-20', 'TT-2026-002', 'Serum Institute of India', '2026-02-17', NULL, NOW()),
(3, 3, 1, '2026-01-10', 'TT-2026-003', 'Serum Institute of India', '2026-02-07', NULL, NOW()),
(4, 1, 1, '2025-12-15', 'TT-2025-089', 'Serum Institute of India', '2026-01-12', NULL, NOW()),
(5, 2, 1, '2025-11-20', 'TT-2025-078', 'Serum Institute of India', '2025-12-18', 'Mild soreness at injection site', NOW());

-- ====================================================
-- 12. BABY_VACCINATION TABLE
-- ====================================================
INSERT INTO BABY_VACCINATION (baby_id, midwife_id, schedule_id, vaccination_date, batch_number, manufacturer, next_dose_date, adverse_reaction, created_at) VALUES
(1, 1, 6, '2025-08-05', 'BCG-2025-150', 'Japan BCG Laboratory', NULL, NULL, NOW()),
(2, 2, 6, '2025-07-20', 'BCG-2025-148', 'Japan BCG Laboratory', NULL, NULL, NOW()),
(3, 3, 6, '2025-06-25', 'BCG-2025-142', 'Japan BCG Laboratory', NULL, 'Small pustule at injection site (normal reaction)', NOW()),
(4, 1, 6, '2025-05-15', 'BCG-2025-135', 'Japan BCG Laboratory', NULL, NULL, NOW()),
(5, 2, 6, '2025-04-10', 'BCG-2025-128', 'Japan BCG Laboratory', NULL, NULL, NOW());

-- ====================================================
-- 13. TRIPOSHA_STOCK TABLE
-- ====================================================
INSERT INTO TRIPOSHA_STOCK (quantity_kg, batch_number, expiry_date, received_date, supplier, created_at, updated_at) VALUES
(500.0, 'TRIP-2026-001', '2027-02-01', '2026-02-01', 'Thriposha Corporation Sri Lanka', NOW(), NOW()),
(600.0, 'TRIP-2026-002', '2027-03-01', '2026-02-10', 'Thriposha Corporation Sri Lanka', NOW(), NOW()),
(450.0, 'TRIP-2025-089', '2026-12-01', '2025-12-01', 'Thriposha Corporation Sri Lanka', NOW(), NOW()),
(550.0, 'TRIP-2025-078', '2026-11-01', '2025-11-01', 'Thriposha Corporation Sri Lanka', NOW(), NOW()),
(700.0, 'TRIP-2026-003', '2027-04-01', '2026-02-15', 'Thriposha Corporation Sri Lanka', NOW(), NOW());

-- ====================================================
-- 14. MOTHER_TRIPOSHA_DISTRIBUTION TABLE
-- ====================================================
INSERT INTO MOTHER_TRIPOSHA_DISTRIBUTION (pregnancy_id, midwife_id, quantity_kg, distribution_date, remarks, created_at) VALUES
(1, 1, 2.5, '2026-02-09', 'Regular monthly distribution', NOW()),
(2, 2, 2.5, '2026-01-20', 'Mother receiving adequate nutrition', NOW()),
(3, 3, 2.5, '2026-01-10', 'Third pregnancy, familiar with triposha', NOW()),
(4, 1, 3.0, '2025-12-15', 'Increased quantity due to high-risk status', NOW()),
(5, 2, 2.5, '2025-11-20', 'Regular distribution', NOW());

-- ====================================================
-- 15. BABY_TRIPOSHA_DISTRIBUTION TABLE
-- ====================================================
INSERT INTO BABY_TRIPOSHA_DISTRIBUTION (baby_id, midwife_id, quantity_kg, distribution_date, remarks, created_at) VALUES
(1, 1, 2.0, '2025-11-05', 'First triposha distribution at 3 months', NOW()),
(2, 2, 2.0, '2025-10-20', 'Regular monthly distribution', NOW()),
(3, 3, 2.0, '2025-09-25', 'Baby recovering well from jaundice', NOW()),
(4, 1, 2.0, '2025-08-15', 'Baby gaining weight excellently', NOW()),
(5, 2, 2.0, '2025-07-10', 'Mother following instructions well', NOW());

-- ====================================================
-- 16. SESSION_TYPE TABLE
-- ====================================================
INSERT INTO SESSION_TYPE (type_name, description, target_audience) VALUES
('Prenatal Care Workshop', 'Comprehensive education on pregnancy care, nutrition, and preparation for childbirth', 'PREGNANT_MOTHERS'),
('Breastfeeding Support', 'Guidance and support on breastfeeding techniques, common challenges, and solutions', 'NEW_MOTHERS'),
('Child Development', 'Information on developmental milestones, growth monitoring, and early childhood care', 'FAMILIES'),
('Nutrition Education', 'Nutritional guidance for mothers during pregnancy and breastfeeding, infant nutrition', 'ALL'),
('Vaccination Awareness', 'Education on immunization schedules, vaccine importance, and safety', 'ALL');

-- ====================================================
-- 17. SESSION TABLE
-- ====================================================
INSERT INTO SESSION (midwife_id, session_type_id, phm_area_id, session_date, start_time, end_time, topic, venue, description, capacity, status, created_at) VALUES
(1, 1, 1, '2026-03-15', '09:00:00', '12:00:00', 'Preparing for Your Baby', 'Colombo Central Community Hall', 'Comprehensive session on labor, delivery, and newborn care basics', 30, 'SCHEDULED', NOW()),
(2, 2, 2, '2026-02-25', '14:00:00', '16:00:00', 'Breastfeeding Success', 'Colombo North Clinic', 'Hands-on breastfeeding workshop with practical demonstrations', 20, 'SCHEDULED', NOW()),
(3, 3, 3, '2026-02-20', '10:00:00', '12:00:00', 'Your Baby First Year', 'Kandy City Medical Center', 'Understanding developmental milestones and growth expectations', 25, 'COMPLETED', NOW()),
(1, 4, 1, '2026-01-30', '09:00:00', '11:00:00', 'Healthy Eating for Two', 'Colombo Central Community Hall', 'Nutritional requirements during pregnancy and meal planning', 35, 'COMPLETED', NOW()),
(2, 5, 2, '2026-01-15', '14:00:00', '16:00:00', 'Protect Your Child', 'Colombo North Clinic', 'Complete vaccination schedule and importance of immunization', 40, 'COMPLETED', NOW());

-- ====================================================
-- 18. SESSION_ATTENDANCE TABLE
-- ====================================================
INSERT INTO SESSION_ATTENDANCE (session_id, mother_id, attended, attendance_time, notes, feedback, created_at) VALUES
(3, 3, TRUE, '2026-02-20 10:05:00', 'Active participation, asked several questions', 'Very informative session, learned a lot', NOW()),
(4, 1, TRUE, '2026-01-30 09:00:00', 'Punctual attendance, took detailed notes', 'Excellent presentation, practical tips were helpful', NOW()),
(4, 2, TRUE, '2026-01-30 09:10:00', 'Brought food diary for review', 'Great session, nutritionist was very knowledgeable', NOW()),
(5, 1, TRUE, '2026-01-15 14:00:00', 'Concerned about vaccine safety', 'Addressed all my concerns, feeling confident now', NOW()),
(5, 4, TRUE, '2026-01-15 14:15:00', 'Took vaccination schedule card', 'Clear explanation of EPI schedule', NOW());

-- ====================================================
-- 19. NOTIFICATION_TYPE TABLE
-- ====================================================
INSERT INTO NOTIFICATION_TYPE (type_name, description, template, priority) VALUES
('Appointment Reminder', 'Reminder for upcoming medical appointments', 'Dear {mother_name}, reminder for your appointment on {date} at {time}.', 'MEDIUM'),
('Vaccination Due', 'Notification for upcoming vaccinations', 'Your child {baby_name} is due for {vaccine_name} on {date}.', 'HIGH'),
('Session Invitation', 'Invitation to health education sessions', 'You are invited to {session_topic} on {date} at {venue}.', 'MEDIUM'),
('Health Alert', 'Urgent health-related notifications', 'IMPORTANT: {message}. Contact your healthcare provider immediately.', 'URGENT'),
('Triposha Collection', 'Notification for nutrition supplement collection', 'Triposha distribution for {baby_name} is available on {date} at {location}.', 'LOW');

-- ====================================================
-- 20. NOTIFICATION TABLE
-- ====================================================
INSERT INTO NOTIFICATION (type_id, midwife_id, mother_id, sent_date, message, status, delivery_method, event_date, event_type, read_at, responded_at, created_at) VALUES
(1, 1, 1, '2026-03-07 09:00:00', 'Dear Priya Silva, reminder for your appointment on 2026-03-09 at 09:00 AM.', 'DELIVERED', 'SMS', '2026-03-09', 'APPOINTMENT', '2026-03-07 10:30:00', NULL, NOW()),
(2, 2, 2, '2026-02-15 10:00:00', 'Your child Baby Fernando is due for DTP vaccination on 2026-02-20.', 'DELIVERED', 'WHATSAPP', '2026-02-20', 'VACCINATION', '2026-02-15 11:20:00', '2026-02-15 14:30:00', NOW()),
(3, 1, 1, '2026-03-05 08:00:00', 'You are invited to Preparing for Your Baby on 2026-03-15 at Colombo Central Community Hall.', 'SENT', 'EMAIL', '2026-03-15', 'SESSION', NULL, NULL, NOW()),
(4, 1, 4, '2026-01-05 14:00:00', 'IMPORTANT: Your blood pressure reading was elevated. Please monitor daily and contact if >140/90.', 'DELIVERED', 'CALL', '2026-01-05', 'HEALTH_ALERT', '2026-01-05 14:15:00', '2026-01-05 15:00:00', NOW()),
(5, 1, 1, '2025-11-28 09:00:00', 'Triposha distribution for Baby Silva is available on 2025-12-05 at Colombo Central Clinic.', 'DELIVERED', 'SMS', '2025-12-05', 'TRIPOSHA', '2025-11-28 16:45:00', NULL, NOW());

-- ====================================================
-- DATA VERIFICATION
-- ====================================================
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
UNION ALL SELECT 'TRIPOSHA_STOCK', COUNT(*) FROM TRIPOSHA_STOCK
UNION ALL SELECT 'MOTHER_TRIPOSHA_DISTRIBUTION', COUNT(*) FROM MOTHER_TRIPOSHA_DISTRIBUTION
UNION ALL SELECT 'BABY_TRIPOSHA_DISTRIBUTION', COUNT(*) FROM BABY_TRIPOSHA_DISTRIBUTION
UNION ALL SELECT 'SESSION_TYPE', COUNT(*) FROM SESSION_TYPE
UNION ALL SELECT 'SESSION', COUNT(*) FROM SESSION
UNION ALL SELECT 'SESSION_ATTENDANCE', COUNT(*) FROM SESSION_ATTENDANCE
UNION ALL SELECT 'NOTIFICATION_TYPE', COUNT(*) FROM NOTIFICATION_TYPE
UNION ALL SELECT 'NOTIFICATION', COUNT(*) FROM NOTIFICATION;

-- ====================================================
-- END OF DATA POPULATION
-- ====================================================
