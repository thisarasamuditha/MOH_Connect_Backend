-- ====================================================
-- MOH Connect Backend - Database Schema
-- ====================================================
-- Database: moh_connect

-- Drop database if exists and create fresh
DROP DATABASE IF EXISTS moh_connect;
CREATE DATABASE moh_connect CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE moh_connect;

-- ====================================================
-- AUTHENTICATION & AUTHORIZATION
-- ====================================================

CREATE TABLE USER (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'MIDWIFE', 'DOCTOR', 'MOTHER') NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role)
) ENGINE=InnoDB;

CREATE TABLE USER_SESSION (
    session_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    token VARCHAR(500) UNIQUE NOT NULL,
    ip_address VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES USER(user_id) ON DELETE CASCADE,
    INDEX idx_token (token),
    INDEX idx_user_session (user_id, expires_at)
) ENGINE=InnoDB;

-- ====================================================
-- REGION & PHM AREA
-- ====================================================

CREATE TABLE PHM_AREA (
    phm_area_id INT AUTO_INCREMENT PRIMARY KEY,
    area_name VARCHAR(255) NOT NULL,
    area_code VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_area_code (area_code)
) ENGINE=InnoDB;

-- ====================================================
-- STAFF (Doctor, Midwife)
-- ====================================================

CREATE TABLE MIDWIFE (
    midwife_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNIQUE NOT NULL,
    phm_area_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    contact_number VARCHAR(20),
    email VARCHAR(255),
    assignment_date DATE,
    qualifications VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES USER(user_id) ON DELETE CASCADE,
    FOREIGN KEY (phm_area_id) REFERENCES PHM_AREA(phm_area_id),
    INDEX idx_phm_area (phm_area_id)
) ENGINE=InnoDB;

CREATE TABLE DOCTOR (
    doctor_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    specialization VARCHAR(255),
    contact_number VARCHAR(20),
    email VARCHAR(255),
    license_number VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES USER(user_id) ON DELETE CASCADE,
    INDEX idx_license (license_number)
) ENGINE=InnoDB;

-- ====================================================
-- FAMILY UNIT (Consolidated)
-- ====================================================

CREATE TABLE FAMILY_UNIT (
    family_unit_id INT AUTO_INCREMENT PRIMARY KEY,
    phm_area_id INT NOT NULL,
    
    -- Family Information
    address TEXT,
    registration_date DATE NOT NULL,
    contact_number VARCHAR(20),
    form_book_number VARCHAR(50) UNIQUE NOT NULL,
    
    -- Husband Information
    husband_nic VARCHAR(20) UNIQUE NOT NULL,
    husband_name VARCHAR(255) NOT NULL,
    husband_dob DATE,
    husband_occupation VARCHAR(255),
    husband_contact VARCHAR(20),
    husband_blood_group VARCHAR(10),
    
    -- Wife Information (Mother)
    wife_user_id INT UNIQUE,
    wife_nic VARCHAR(20) UNIQUE NOT NULL,
    wife_name VARCHAR(255) NOT NULL,
    wife_dob DATE,
    wife_occupation VARCHAR(255),
    wife_contact VARCHAR(20),
    wife_blood_group VARCHAR(10),
    
    -- Marriage Information
    marriage_date DATE,
    marriage_certificate_number VARCHAR(100),
    
    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (phm_area_id) REFERENCES PHM_AREA(phm_area_id),
    FOREIGN KEY (wife_user_id) REFERENCES USER(user_id) ON DELETE SET NULL,
    INDEX idx_phm_wife (phm_area_id, wife_nic),
    INDEX idx_husband_nic (husband_nic),
    INDEX idx_form_book (form_book_number),
    INDEX idx_wife_user (wife_user_id)
) ENGINE=InnoDB;

-- ====================================================
-- PREGNANCY & BABY
-- ====================================================

CREATE TABLE PREGNANCY (
    pregnancy_id INT AUTO_INCREMENT PRIMARY KEY,
    family_unit_id INT NOT NULL,
    pregnancy_number VARCHAR(50) UNIQUE NOT NULL,
    
    -- Pregnancy Dates
    lmp_date DATE NOT NULL,
    edd_date DATE NOT NULL,
    delivery_date DATE,
    delivery_type ENUM('NORMAL', 'C_SECTION', 'ASSISTED', 'OTHER'),
    
    -- Pregnancy Status
    pregnancy_status ENUM('ACTIVE', 'COMPLETED', 'TERMINATED', 'MISCARRIAGE') DEFAULT 'ACTIVE',
    gravida INT DEFAULT 1,
    para INT DEFAULT 0,
    
    -- Risk Assessment
    risk_level ENUM('LOW', 'MEDIUM', 'HIGH') DEFAULT 'LOW',
    risk_factors TEXT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (family_unit_id) REFERENCES FAMILY_UNIT(family_unit_id) ON DELETE CASCADE,
    INDEX idx_family_status (family_unit_id, pregnancy_status),
    INDEX idx_edd_date (edd_date)
) ENGINE=InnoDB;

CREATE TABLE BABY (
    baby_id INT AUTO_INCREMENT PRIMARY KEY,
    pregnancy_id INT NOT NULL,
    family_unit_id INT NOT NULL,
    
    name VARCHAR(255),
    date_of_birth DATE NOT NULL,
    gender ENUM('MALE', 'FEMALE') NOT NULL,
    birth_weight FLOAT COMMENT 'kg',
    birth_height FLOAT COMMENT 'cm',
    birth_complications TEXT,
    
    -- Additional Info
    apgar_score VARCHAR(20),
    birth_order INT DEFAULT 1,
    is_alive BOOLEAN DEFAULT TRUE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (pregnancy_id) REFERENCES PREGNANCY(pregnancy_id) ON DELETE CASCADE,
    FOREIGN KEY (family_unit_id) REFERENCES FAMILY_UNIT(family_unit_id) ON DELETE CASCADE,
    INDEX idx_family_unit (family_unit_id),
    INDEX idx_pregnancy (pregnancy_id),
    INDEX idx_dob (date_of_birth)
) ENGINE=InnoDB;

-- ====================================================
-- MEDICAL RECORDS
-- ====================================================

CREATE TABLE MOTHER_RECORD (
    record_id INT AUTO_INCREMENT PRIMARY KEY,
    pregnancy_id INT NOT NULL,
    midwife_id INT,
    doctor_id INT,
    
    record_date DATE NOT NULL,
    gestational_age INT COMMENT 'weeks',
    
    -- Vitals
    weight FLOAT COMMENT 'kg',
    bmi FLOAT,
    blood_pressure VARCHAR(20),
    shf FLOAT COMMENT 'Symphysis-fundal height',
    
    -- Clinical Findings
    findings TEXT,
    recommendations TEXT,
    complications TEXT,
    notes TEXT,
    
    -- Follow-up
    next_visit_date DATE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (pregnancy_id) REFERENCES PREGNANCY(pregnancy_id) ON DELETE CASCADE,
    FOREIGN KEY (midwife_id) REFERENCES MIDWIFE(midwife_id) ON DELETE SET NULL,
    FOREIGN KEY (doctor_id) REFERENCES DOCTOR(doctor_id) ON DELETE SET NULL,
    INDEX idx_pregnancy_date (pregnancy_id, record_date)
) ENGINE=InnoDB;

CREATE TABLE BABY_RECORD (
    record_id INT AUTO_INCREMENT PRIMARY KEY,
    baby_id INT NOT NULL,
    midwife_id INT,
    doctor_id INT,
    
    record_date DATE NOT NULL,
    age_months INT,
    
    -- Growth Parameters
    weight FLOAT COMMENT 'kg',
    height FLOAT COMMENT 'cm',
    head_circumference FLOAT COMMENT 'cm',
    bmi FLOAT,
    
    -- Development
    developmental_milestones TEXT,
    growth_status ENUM('NORMAL', 'UNDERWEIGHT', 'OVERWEIGHT', 'STUNTED', 'WASTED') DEFAULT 'NORMAL',
    
    -- Clinical
    findings TEXT,
    recommendations TEXT,
    health_status VARCHAR(255),
    notes TEXT,
    
    -- Follow-up
    next_visit_date DATE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (baby_id) REFERENCES BABY(baby_id) ON DELETE CASCADE,
    FOREIGN KEY (midwife_id) REFERENCES MIDWIFE(midwife_id) ON DELETE SET NULL,
    FOREIGN KEY (doctor_id) REFERENCES DOCTOR(doctor_id) ON DELETE SET NULL,
    INDEX idx_baby_date (baby_id, record_date)
) ENGINE=InnoDB;

-- ====================================================
-- VACCINATIONS
-- ====================================================

CREATE TABLE VACCINE_SCHEDULE (
    schedule_id INT AUTO_INCREMENT PRIMARY KEY,
    vaccine_name VARCHAR(255) NOT NULL,
    target_group ENUM('MOTHER', 'BABY') NOT NULL,
    dose_number INT NOT NULL,
    recommended_age_days INT NOT NULL,
    description TEXT,
    INDEX idx_target_group (target_group)
) ENGINE=InnoDB;

CREATE TABLE MOTHER_VACCINATION (
    vaccination_id INT AUTO_INCREMENT PRIMARY KEY,
    pregnancy_id INT NOT NULL,
    midwife_id INT,
    schedule_id INT NOT NULL,
    
    vaccination_date DATE NOT NULL,
    batch_number VARCHAR(100),
    manufacturer VARCHAR(255),
    next_dose_date DATE,
    
    -- Side Effects
    adverse_reaction TEXT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (pregnancy_id) REFERENCES PREGNANCY(pregnancy_id) ON DELETE CASCADE,
    FOREIGN KEY (midwife_id) REFERENCES MIDWIFE(midwife_id) ON DELETE SET NULL,
    FOREIGN KEY (schedule_id) REFERENCES VACCINE_SCHEDULE(schedule_id),
    INDEX idx_pregnancy_date (pregnancy_id, vaccination_date)
) ENGINE=InnoDB;

CREATE TABLE BABY_VACCINATION (
    vaccination_id INT AUTO_INCREMENT PRIMARY KEY,
    baby_id INT NOT NULL,
    midwife_id INT,
    schedule_id INT NOT NULL,
    
    vaccination_date DATE NOT NULL,
    batch_number VARCHAR(100),
    manufacturer VARCHAR(255),
    next_dose_date DATE,
    
    -- Side Effects
    adverse_reaction TEXT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (baby_id) REFERENCES BABY(baby_id) ON DELETE CASCADE,
    FOREIGN KEY (midwife_id) REFERENCES MIDWIFE(midwife_id) ON DELETE SET NULL,
    FOREIGN KEY (schedule_id) REFERENCES VACCINE_SCHEDULE(schedule_id),
    INDEX idx_baby_date (baby_id, vaccination_date)
) ENGINE=InnoDB;

-- ====================================================
-- MEDICATION
-- ====================================================

CREATE TABLE MEDICATION (
    medication_id INT AUTO_INCREMENT PRIMARY KEY,
    pregnancy_id INT NOT NULL,
    midwife_id INT,
    doctor_id INT,
    
    medication_name VARCHAR(255) NOT NULL,
    dosage VARCHAR(100),
    start_date DATE NOT NULL,
    end_date DATE,
    frequency VARCHAR(100),
    route ENUM('ORAL', 'INJECTION', 'TOPICAL', 'OTHER') DEFAULT 'ORAL',
    
    indication TEXT,
    notes TEXT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (pregnancy_id) REFERENCES PREGNANCY(pregnancy_id) ON DELETE CASCADE,
    FOREIGN KEY (midwife_id) REFERENCES MIDWIFE(midwife_id) ON DELETE SET NULL,
    FOREIGN KEY (doctor_id) REFERENCES DOCTOR(doctor_id) ON DELETE SET NULL,
    INDEX idx_pregnancy_start (pregnancy_id, start_date)
) ENGINE=InnoDB;

-- ====================================================
-- TRIPOSHA DISTRIBUTION
-- ====================================================

CREATE TABLE TRIPOSHA_DISTRIBUTION (
    distribution_id INT AUTO_INCREMENT PRIMARY KEY,
    baby_id INT NOT NULL,
    midwife_id INT,
    
    distribution_date DATE NOT NULL,
    quantity FLOAT COMMENT 'kg',
    packet_type ENUM('STANDARD', 'SPECIAL') DEFAULT 'STANDARD',
    next_distribution_date DATE,
    
    beneficiary_signature TEXT,
    notes TEXT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (baby_id) REFERENCES BABY(baby_id) ON DELETE CASCADE,
    FOREIGN KEY (midwife_id) REFERENCES MIDWIFE(midwife_id) ON DELETE SET NULL,
    INDEX idx_baby_date (baby_id, distribution_date)
) ENGINE=InnoDB;

-- ====================================================
-- SESSION SYSTEM
-- ====================================================

CREATE TABLE SESSION_TYPE (
    session_type_id INT AUTO_INCREMENT PRIMARY KEY,
    type_name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    target_audience ENUM('PREGNANT_MOTHERS', 'NEW_MOTHERS', 'FAMILIES', 'ALL') DEFAULT 'ALL'
) ENGINE=InnoDB;

CREATE TABLE SESSION (
    session_id INT AUTO_INCREMENT PRIMARY KEY,
    midwife_id INT NOT NULL,
    session_type_id INT NOT NULL,
    phm_area_id INT NOT NULL,
    
    session_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME,
    topic VARCHAR(500) NOT NULL,
    venue VARCHAR(500),
    description TEXT,
    capacity INT DEFAULT 50,
    
    status ENUM('SCHEDULED', 'COMPLETED', 'CANCELLED') DEFAULT 'SCHEDULED',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (midwife_id) REFERENCES MIDWIFE(midwife_id) ON DELETE CASCADE,
    FOREIGN KEY (session_type_id) REFERENCES SESSION_TYPE(session_type_id),
    FOREIGN KEY (phm_area_id) REFERENCES PHM_AREA(phm_area_id),
    INDEX idx_date_area (session_date, phm_area_id),
    INDEX idx_midwife_date (midwife_id, session_date)
) ENGINE=InnoDB;

CREATE TABLE SESSION_ATTENDANCE (
    attendance_id INT AUTO_INCREMENT PRIMARY KEY,
    session_id INT NOT NULL,
    family_unit_id INT NOT NULL,
    
    attended BOOLEAN DEFAULT FALSE,
    attendance_time TIMESTAMP NULL,
    notes TEXT,
    feedback TEXT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (session_id) REFERENCES SESSION(session_id) ON DELETE CASCADE,
    FOREIGN KEY (family_unit_id) REFERENCES FAMILY_UNIT(family_unit_id) ON DELETE CASCADE,
    INDEX idx_session (session_id),
    UNIQUE KEY unique_attendance (session_id, family_unit_id)
) ENGINE=InnoDB;

-- ====================================================
-- NOTIFICATION SYSTEM
-- ====================================================

CREATE TABLE NOTIFICATION_TYPE (
    type_id INT AUTO_INCREMENT PRIMARY KEY,
    type_name VARCHAR(255) UNIQUE NOT NULL,
    template TEXT,
    description TEXT,
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM'
) ENGINE=InnoDB;

CREATE TABLE NOTIFICATION (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    type_id INT NOT NULL,
    midwife_id INT,
    family_unit_id INT NOT NULL,
    
    sent_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    message TEXT NOT NULL,
    status ENUM('PENDING', 'SENT', 'DELIVERED', 'FAILED') DEFAULT 'PENDING',
    delivery_method ENUM('SMS', 'EMAIL', 'WHATSAPP', 'CALL'),
    
    -- Related Event
    event_date DATE,
    event_type VARCHAR(255),
    
    -- Tracking
    read_at TIMESTAMP NULL,
    responded_at TIMESTAMP NULL,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (type_id) REFERENCES NOTIFICATION_TYPE(type_id),
    FOREIGN KEY (midwife_id) REFERENCES MIDWIFE(midwife_id) ON DELETE SET NULL,
    FOREIGN KEY (family_unit_id) REFERENCES FAMILY_UNIT(family_unit_id) ON DELETE CASCADE,
    INDEX idx_family_sent (family_unit_id, sent_date),
    INDEX idx_status_sent (status, sent_date)
) ENGINE=InnoDB;

-- ====================================================
-- AUDIT LOG
-- ====================================================

CREATE TABLE AUDIT_LOG (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    action VARCHAR(255) NOT NULL,
    table_name VARCHAR(100) NOT NULL,
    record_id INT,
    old_value TEXT,
    new_value TEXT,
    ip_address VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES USER(user_id) ON DELETE SET NULL,
    INDEX idx_user_created (user_id, created_at),
    INDEX idx_table_record (table_name, record_id)
) ENGINE=InnoDB;

-- ====================================================
-- END OF SCHEMA
-- ====================================================
