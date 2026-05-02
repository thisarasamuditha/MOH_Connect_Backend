-- ====================================================
-- MOH Connect Backend - Database Schema
-- Refactored to match Java Entity Models exactly
-- ====================================================
-- Generated: February 20, 2026
-- ====================================================

DROP DATABASE IF EXISTS moh_db;
CREATE DATABASE moh_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE moh_db;

-- ====================================================
-- AUTHENTICATION & USER MANAGEMENT
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================
-- REGION & PHM AREA
-- ====================================================

CREATE TABLE PHM_AREA (
    phm_area_id INT AUTO_INCREMENT PRIMARY KEY,
    area_name VARCHAR(255) NOT NULL,
    area_code VARCHAR(50) UNIQUE NOT NULL,

    INDEX idx_area_code (area_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
    FOREIGN KEY (phm_area_id) REFERENCES PHM_AREA(phm_area_id) ON DELETE RESTRICT,

    INDEX idx_user_id (user_id),
    INDEX idx_phm_area (phm_area_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE SECTION (
    section_id INT AUTO_INCREMENT PRIMARY KEY,
    midwife_id INT NOT NULL,
    phm_area_id INT NOT NULL,
    section_name VARCHAR(255) NOT NULL,
    section_type VARCHAR(50),
    description TEXT,
    location VARCHAR(255),
    capacity INT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (midwife_id) REFERENCES MIDWIFE(midwife_id) ON DELETE RESTRICT,
    FOREIGN KEY (phm_area_id) REFERENCES PHM_AREA(phm_area_id) ON DELETE RESTRICT,

    INDEX idx_midwife_id (midwife_id),
    INDEX idx_phm_area_id (phm_area_id),
    INDEX idx_section_name (section_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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

    INDEX idx_user_id (user_id),
    INDEX idx_license_number (license_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================
-- MOTHER ENTITY
-- ====================================================

CREATE TABLE MOTHER (
    mother_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNIQUE NOT NULL,
    phm_area_id INT NOT NULL,
    address TEXT,
    nic VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(50) NOT NULL,
    date_of_birth DATE,
    occupation VARCHAR(50),
    contact_number VARCHAR(20),
    blood_group VARCHAR(10),
    registration_date DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES USER(user_id) ON DELETE CASCADE,
    FOREIGN KEY (phm_area_id) REFERENCES PHM_AREA(phm_area_id) ON DELETE RESTRICT,

    INDEX idx_user_id (user_id),
    INDEX idx_nic (nic),
    INDEX idx_phm_area (phm_area_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================
-- PREGNANCY & BABIES
-- ====================================================

CREATE TABLE PREGNANCY (
    pregnancy_id INT AUTO_INCREMENT PRIMARY KEY,
    mother_id INT NOT NULL,
    pregnancy_number VARCHAR(50) UNIQUE NOT NULL,
    lmp_date DATE NOT NULL,
    edd_date DATE NOT NULL,
    delivery_date DATE,
    delivery_type ENUM('NORMAL', 'C_SECTION', 'ASSISTED', 'OTHER'),
    pregnancy_status ENUM('ACTIVE', 'COMPLETED', 'TERMINATED', 'MISCARRIAGE') DEFAULT 'ACTIVE',
    gravida INT DEFAULT 1,
    para INT DEFAULT 0,
    risk_level ENUM('LOW', 'MEDIUM', 'HIGH') DEFAULT 'LOW',
    risk_factors TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (mother_id) REFERENCES MOTHER(mother_id) ON DELETE CASCADE,

    INDEX idx_mother_id (mother_id),
    INDEX idx_mother_status (mother_id, pregnancy_status),
    INDEX idx_pregnancy_number (pregnancy_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE BABY (
    baby_id INT AUTO_INCREMENT PRIMARY KEY,
    pregnancy_id INT NOT NULL,
    mother_id INT NOT NULL,
    name VARCHAR(255),
    date_of_birth DATE NOT NULL,
    gender ENUM('MALE', 'FEMALE') NOT NULL,
    birth_weight FLOAT,
    birth_height FLOAT,
    birth_complications TEXT,
    apgar_score VARCHAR(20),
    birth_order INT DEFAULT 1,
    is_alive BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (pregnancy_id) REFERENCES PREGNANCY(pregnancy_id) ON DELETE CASCADE,
    FOREIGN KEY (mother_id) REFERENCES MOTHER(mother_id) ON DELETE CASCADE,

    INDEX idx_pregnancy_id (pregnancy_id),
    INDEX idx_mother_id (mother_id),
    INDEX idx_date_of_birth (date_of_birth)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================
-- MEDICAL RECORDS
-- ====================================================

CREATE TABLE MOTHER_RECORD (
    record_id INT AUTO_INCREMENT PRIMARY KEY,
    pregnancy_id INT NOT NULL,
    midwife_id INT,
    doctor_id INT,
    record_date DATE NOT NULL,
    gestational_age INT,
    weight FLOAT,
    bmi FLOAT,
    blood_pressure VARCHAR(50),
    shf FLOAT,
    findings TEXT,
    recommendations TEXT,
    complications TEXT,
    notes TEXT,
    next_visit_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (pregnancy_id) REFERENCES PREGNANCY(pregnancy_id) ON DELETE CASCADE,
    FOREIGN KEY (midwife_id) REFERENCES MIDWIFE(midwife_id) ON DELETE SET NULL,
    FOREIGN KEY (doctor_id) REFERENCES DOCTOR(doctor_id) ON DELETE SET NULL,

    INDEX idx_pregnancy_id (pregnancy_id),
    INDEX idx_record_date (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE BABY_RECORD (
    record_id INT AUTO_INCREMENT PRIMARY KEY,
    baby_id INT NOT NULL,
    midwife_id INT,
    doctor_id INT,
    record_date DATE NOT NULL,
    age_months INT,
    weight FLOAT,
    height FLOAT,
    head_circumference FLOAT,
    bmi FLOAT,
    developmental_milestones TEXT,
    growth_status ENUM('NORMAL', 'UNDERWEIGHT', 'OVERWEIGHT', 'STUNTED', 'WASTED'),
    findings TEXT,
    recommendations TEXT,
    health_status VARCHAR(255),
    notes TEXT,
    next_visit_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (baby_id) REFERENCES BABY(baby_id) ON DELETE CASCADE,
    FOREIGN KEY (midwife_id) REFERENCES MIDWIFE(midwife_id) ON DELETE SET NULL,
    FOREIGN KEY (doctor_id) REFERENCES DOCTOR(doctor_id) ON DELETE SET NULL,

    INDEX idx_baby_id (baby_id),
    INDEX idx_record_date (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================
-- VACCINATION SYSTEM
-- ====================================================

CREATE TABLE VACCINE_SCHEDULE (
    schedule_id INT AUTO_INCREMENT PRIMARY KEY,
    vaccine_name VARCHAR(255) NOT NULL,
    target_group ENUM('MOTHER', 'BABY') NOT NULL,
    dose_number INT NOT NULL,
    recommended_age_days INT,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_target_group (target_group),
    INDEX idx_vaccine_name (vaccine_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE MOTHER_VACCINATION (
    vaccination_id INT AUTO_INCREMENT PRIMARY KEY,
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

    INDEX idx_pregnancy_id (pregnancy_id),
    INDEX idx_vaccination_date (vaccination_date),
    INDEX idx_schedule_id (schedule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE BABY_VACCINATION (
    vaccination_id INT AUTO_INCREMENT PRIMARY KEY,
    baby_id INT NOT NULL,
    midwife_id INT,
    schedule_id INT NOT NULL,
    vaccination_date DATE NOT NULL,
    batch_number VARCHAR(100),
    manufacturer VARCHAR(255),
    next_dose_date DATE,
    adverse_reaction TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (baby_id) REFERENCES BABY(baby_id) ON DELETE CASCADE,
    FOREIGN KEY (midwife_id) REFERENCES MIDWIFE(midwife_id) ON DELETE SET NULL,
    FOREIGN KEY (schedule_id) REFERENCES VACCINE_SCHEDULE(schedule_id) ON DELETE RESTRICT,

    INDEX idx_baby_id (baby_id),
    INDEX idx_vaccination_date (vaccination_date),
    INDEX idx_schedule_id (schedule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================
-- TRIPOSHA DISTRIBUTION SYSTEM
-- ====================================================

CREATE TABLE TRIPOSHA_STOCK (
    stock_id INT AUTO_INCREMENT PRIMARY KEY,
    quantity_kg DOUBLE NOT NULL,
    batch_number VARCHAR(100) UNIQUE NOT NULL,
    expiry_date DATE NOT NULL,
    received_date DATE NOT NULL,
    supplier VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_batch_number (batch_number),
    INDEX idx_expiry_date (expiry_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE MOTHER_TRIPOSHA_DISTRIBUTION (
    distribution_id INT AUTO_INCREMENT PRIMARY KEY,
    pregnancy_id INT NOT NULL,
    midwife_id INT NOT NULL,
    quantity_kg DOUBLE NOT NULL,
    distribution_date DATE NOT NULL,
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (pregnancy_id) REFERENCES PREGNANCY(pregnancy_id) ON DELETE CASCADE,
    FOREIGN KEY (midwife_id) REFERENCES MIDWIFE(midwife_id) ON DELETE RESTRICT,

    INDEX idx_pregnancy_id (pregnancy_id),
    INDEX idx_distribution_date (distribution_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE BABY_TRIPOSHA_DISTRIBUTION (
    distribution_id INT AUTO_INCREMENT PRIMARY KEY,
    baby_id INT NOT NULL,
    midwife_id INT NOT NULL,
    quantity_kg DOUBLE NOT NULL,
    distribution_date DATE NOT NULL,
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (baby_id) REFERENCES BABY(baby_id) ON DELETE CASCADE,
    FOREIGN KEY (midwife_id) REFERENCES MIDWIFE(midwife_id) ON DELETE RESTRICT,

    INDEX idx_baby_id (baby_id),
    INDEX idx_distribution_date (distribution_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================
-- SESSION SYSTEM
-- ====================================================

CREATE TABLE SESSION_TYPE (
    session_type_id INT AUTO_INCREMENT PRIMARY KEY,
    type_name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    target_audience ENUM('PREGNANT_MOTHERS', 'NEW_MOTHERS', 'FAMILIES', 'ALL'),

    INDEX idx_type_name (type_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE SESSION (
    session_id INT AUTO_INCREMENT PRIMARY KEY,
    midwife_id INT NOT NULL,
    session_type_id INT NOT NULL,
    phm_area_id INT NOT NULL,
    session_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME,
    topic VARCHAR(255),
    venue VARCHAR(255),
    description TEXT,
    capacity INT,
    status ENUM('SCHEDULED', 'COMPLETED', 'CANCELLED'),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (midwife_id) REFERENCES MIDWIFE(midwife_id) ON DELETE RESTRICT,
    FOREIGN KEY (session_type_id) REFERENCES SESSION_TYPE(session_type_id) ON DELETE RESTRICT,
    FOREIGN KEY (phm_area_id) REFERENCES PHM_AREA(phm_area_id) ON DELETE RESTRICT,

    INDEX idx_session_date (session_date),
    INDEX idx_midwife_id (midwife_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE SESSION_ATTENDANCE (
    attendance_id INT AUTO_INCREMENT PRIMARY KEY,
    session_id INT NOT NULL,
    mother_id INT NOT NULL,
    attended BOOLEAN DEFAULT FALSE,
    attendance_time TIMESTAMP NULL,
    notes TEXT,
    feedback TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (session_id) REFERENCES SESSION(session_id) ON DELETE CASCADE,
    FOREIGN KEY (mother_id) REFERENCES MOTHER(mother_id) ON DELETE CASCADE,

    INDEX idx_session_id (session_id),
    INDEX idx_mother_id (mother_id),
    UNIQUE KEY unique_session_mother (session_id, mother_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================
-- NOTIFICATION SYSTEM
-- ====================================================

CREATE TABLE NOTIFICATION_TYPE (
    type_id INT AUTO_INCREMENT PRIMARY KEY,
    type_name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    template TEXT,
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT'),

    INDEX idx_type_name (type_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE NOTIFICATION (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    type_id INT NOT NULL,
    midwife_id INT,
    mother_id INT NOT NULL,
    sent_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    message TEXT NOT NULL,
    status ENUM('PENDING', 'SENT', 'DELIVERED', 'FAILED') DEFAULT 'PENDING',
    delivery_method ENUM('SMS', 'EMAIL', 'WHATSAPP', 'CALL'),
    event_date DATE,
    event_type VARCHAR(255),
    read_at TIMESTAMP NULL,
    responded_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (type_id) REFERENCES NOTIFICATION_TYPE(type_id) ON DELETE RESTRICT,
    FOREIGN KEY (midwife_id) REFERENCES MIDWIFE(midwife_id) ON DELETE SET NULL,
    FOREIGN KEY (mother_id) REFERENCES MOTHER(mother_id) ON DELETE CASCADE,

    INDEX idx_mother_id (mother_id),
    INDEX idx_mother_sent (mother_id, sent_date),
    INDEX idx_status (status),
    INDEX idx_event_date (event_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================
-- SCHEMA CREATION COMPLETE
-- ====================================================

-- Display created tables
SHOW TABLES;

-- Display table structure for verification
SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    CREATE_TIME
FROM 
    INFORMATION_SCHEMA.TABLES
WHERE 
    TABLE_SCHEMA = 'moh_db'
ORDER BY 
    TABLE_NAME;
