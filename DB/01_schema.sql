-- ====================================================
-- MOH Connect Backend - Database Schema (Updated with MOTHER entity)
-- ====================================================

DROP DATABASE IF EXISTS moh_connect;
CREATE DATABASE moh_connect CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE moh_connect;

-- ====================================================
-- AUTHENTICATION & USER MANAGEMENT
-- ====================================================

CREATE TABLE USER (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN','MIDWIFE','DOCTOR','MOTHER') NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,

    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role)
);

-- ====================================================
-- REGION & PHM AREA
-- ====================================================

CREATE TABLE PHM_AREA (
    phm_area_id INT AUTO_INCREMENT PRIMARY KEY,
    area_name VARCHAR(20) NOT NULL,
    area_code VARCHAR(20) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_area_code (area_code)
);

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
);

CREATE TABLE DOCTOR (
    doctor_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    specialization VARCHAR(255),
    contact_number VARCHAR(20),
    email VARCHAR(255),
    license_number VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES USER(user_id) ON DELETE CASCADE
);

-- ====================================================
-- MOTHER ENTITY (Replaces FAMILY UNIT + FAMILY_MEMBER)
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
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES USER(user_id) ON DELETE CASCADE,
    FOREIGN KEY (phm_area_id) REFERENCES PHM_AREA(phm_area_id),

    INDEX idx_nic (nic),
    INDEX idx_phm_area (phm_area_id)
);

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
    delivery_type ENUM('NORMAL','C_SECTION','ASSISTED','OTHER'),

    pregnancy_status ENUM('ACTIVE','COMPLETED','TERMINATED','MISCARRIAGE') DEFAULT 'ACTIVE',
    gravida INT DEFAULT 1,
    para INT DEFAULT 0,

    risk_level ENUM('LOW','MEDIUM','HIGH') DEFAULT 'LOW',
    risk_factors TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (mother_id) REFERENCES MOTHER(mother_id) ON DELETE CASCADE,

    INDEX idx_mother_status (mother_id, pregnancy_status)
);

CREATE TABLE BABY (
    baby_id INT AUTO_INCREMENT PRIMARY KEY,
    pregnancy_id INT NOT NULL,
    mother_id INT NOT NULL,

    name VARCHAR(255),
    date_of_birth DATE NOT NULL,
    gender ENUM('MALE','FEMALE') NOT NULL,
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

    INDEX idx_mother (mother_id),
    INDEX idx_pregnancy (pregnancy_id)
);

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
    blood_pressure VARCHAR(20),
    shf FLOAT,

    findings TEXT,
    recommendations TEXT,
    complications TEXT,
    notes TEXT,

    next_visit_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (pregnancy_id) REFERENCES PREGNANCY(pregnancy_id) ON DELETE CASCADE,
    FOREIGN KEY (midwife_id) REFERENCES MIDWIFE(midwife_id) ON DELETE SET NULL,
    FOREIGN KEY (doctor_id) REFERENCES DOCTOR(doctor_id) ON DELETE SET NULL
);

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
    growth_status ENUM('NORMAL','UNDERWEIGHT','OVERWEIGHT','STUNTED','WASTED'),

    findings TEXT,
    recommendations TEXT,
    health_status VARCHAR(255),
    notes TEXT,

    next_visit_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (baby_id) REFERENCES BABY(baby_id) ON DELETE CASCADE,
    FOREIGN KEY (midwife_id) REFERENCES MIDWIFE(midwife_id) ON DELETE SET NULL,
    FOREIGN KEY (doctor_id) REFERENCES DOCTOR(doctor_id) ON DELETE SET NULL
);

-- ====================================================
-- VACCINATIONS
-- ====================================================

CREATE TABLE VACCINE_SCHEDULE (
    schedule_id INT AUTO_INCREMENT PRIMARY KEY,
    vaccine_name VARCHAR(255),
    target_group ENUM('MOTHER','BABY'),
    dose_number INT,
    recommended_age_days INT,
    description TEXT
);

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
    FOREIGN KEY (schedule_id) REFERENCES VACCINE_SCHEDULE(schedule_id)
);

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
    FOREIGN KEY (schedule_id) REFERENCES VACCINE_SCHEDULE(schedule_id)
);

-- ====================================================
-- MEDICATION
-- ====================================================

CREATE TABLE MEDICATION (
    medication_id INT AUTO_INCREMENT PRIMARY KEY,
    pregnancy_id INT NOT NULL,
    midwife_id INT,
    doctor_id INT,

    medication_name VARCHAR(255),
    dosage VARCHAR(100),
    start_date DATE NOT NULL,
    end_date DATE,
    frequency VARCHAR(100),
    route ENUM('ORAL','INJECTION','TOPICAL','OTHER'),
    indication TEXT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (pregnancy_id) REFERENCES PREGNANCY(pregnancy_id) ON DELETE CASCADE,
    FOREIGN KEY (midwife_id) REFERENCES MIDWIFE(midwife_id) ON DELETE SET NULL,
    FOREIGN KEY (doctor_id) REFERENCES DOCTOR(doctor_id) ON DELETE SET NULL
);

-- ====================================================
-- TRIPOSHA STOCK & DISTRIBUTION (v2)
-- ====================================================

-- Legacy table kept for historical data
CREATE TABLE TRIPOSHA_DISTRIBUTION (
    distribution_id INT AUTO_INCREMENT PRIMARY KEY,
    baby_id INT NOT NULL,
    midwife_id INT,

    distribution_date DATE NOT NULL,
    quantity FLOAT,
    packet_type ENUM('STANDARD','SPECIAL'),
    next_distribution_date DATE,

    beneficiary_signature TEXT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (baby_id) REFERENCES BABY(baby_id) ON DELETE CASCADE,
    FOREIGN KEY (midwife_id) REFERENCES MIDWIFE(midwife_id) ON DELETE SET NULL
);

-- Stock management
CREATE TABLE TRIPOSHA_STOCK (
    stock_id INT AUTO_INCREMENT PRIMARY KEY,
    quantity_kg DOUBLE NOT NULL,
    batch_number VARCHAR(100) UNIQUE NOT NULL,
    expiry_date DATE NOT NULL,
    received_date DATE NOT NULL,
    supplier VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Mother-level distribution linked to pregnancy
CREATE TABLE MOTHER_TRIPOSHA_DISTRIBUTION (
    distribution_id INT AUTO_INCREMENT PRIMARY KEY,
    pregnancy_id INT NOT NULL,
    midwife_id INT NOT NULL,
    quantity_kg DOUBLE NOT NULL,
    distribution_date DATE NOT NULL,
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (pregnancy_id) REFERENCES PREGNANCY(pregnancy_id) ON DELETE CASCADE,
    FOREIGN KEY (midwife_id) REFERENCES MIDWIFE(midwife_id) ON DELETE CASCADE,

    INDEX idx_pregnancy (pregnancy_id),
    INDEX idx_midwife (midwife_id)
);

-- Baby-level distribution
CREATE TABLE BABY_TRIPOSHA_DISTRIBUTION (
    distribution_id INT AUTO_INCREMENT PRIMARY KEY,
    baby_id INT NOT NULL,
    midwife_id INT NOT NULL,
    quantity_kg DOUBLE NOT NULL,
    distribution_date DATE NOT NULL,
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (baby_id) REFERENCES BABY(baby_id) ON DELETE CASCADE,
    FOREIGN KEY (midwife_id) REFERENCES MIDWIFE(midwife_id) ON DELETE CASCADE,

    INDEX idx_baby (baby_id),
    INDEX idx_midwife_baby (midwife_id)
);

-- ====================================================
-- SESSION SYSTEM
-- ====================================================

CREATE TABLE SESSION_TYPE (
    session_type_id INT AUTO_INCREMENT PRIMARY KEY,
    type_name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    target_audience ENUM('PREGNANT_MOTHERS','NEW_MOTHERS','FAMILIES','ALL')
);

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
    status ENUM('SCHEDULED','COMPLETED','CANCELLED'),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (midwife_id) REFERENCES MIDWIFE(midwife_id),
    FOREIGN KEY (session_type_id) REFERENCES SESSION_TYPE(session_type_id),
    FOREIGN KEY (phm_area_id) REFERENCES PHM_AREA(phm_area_id)
);

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

    INDEX idx_session (session_id)
);

-- ====================================================
-- NOTIFICATION SYSTEM
-- ====================================================

CREATE TABLE NOTIFICATION_TYPE (
    type_id INT AUTO_INCREMENT PRIMARY KEY,
    type_name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    template TEXT,
    priority ENUM('LOW','MEDIUM','HIGH','URGENT')
);

CREATE TABLE NOTIFICATION (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    type_id INT NOT NULL,
    midwife_id INT,
    mother_id INT NOT NULL,

    sent_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    message TEXT NOT NULL,
    status ENUM('PENDING','SENT','DELIVERED','FAILED'),
    delivery_method ENUM('SMS','EMAIL','WHATSAPP','CALL'),

    event_date DATE,
    event_type VARCHAR(255),

    read_at TIMESTAMP NULL,
    responded_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (type_id) REFERENCES NOTIFICATION_TYPE(type_id),
    FOREIGN KEY (midwife_id) REFERENCES MIDWIFE(midwife_id),
    FOREIGN KEY (mother_id) REFERENCES MOTHER(mother_id),

    INDEX idx_mother_sent (mother_id, sent_date)
);
