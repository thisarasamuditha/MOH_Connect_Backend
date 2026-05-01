-- ================================================================
-- VACCINATION SYSTEM SCHEMA
-- ================================================================

-- Vaccine Schedule Table
CREATE TABLE IF NOT EXISTS VACCINE_SCHEDULE (
    schedule_id INT PRIMARY KEY AUTO_INCREMENT,
    vaccine_name VARCHAR(255) NOT NULL,
    target_group ENUM('MOTHER', 'BABY') NOT NULL,
    dose_number INT NOT NULL,
    recommended_age_days INT,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_target_group (target_group),
    INDEX idx_recommended_age (recommended_age_days)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Mother Vaccination Table
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

-- Baby Vaccination Table
CREATE TABLE IF NOT EXISTS BABY_VACCINATION (
    vaccination_id INT PRIMARY KEY AUTO_INCREMENT,
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
    INDEX idx_baby (baby_id),
    INDEX idx_vaccination_date (vaccination_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
