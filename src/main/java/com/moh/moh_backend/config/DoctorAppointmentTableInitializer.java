package com.moh.moh_backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DoctorAppointmentTableInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS DOCTOR_APPOINTMENT (" +
                    "    appointment_id          INT AUTO_INCREMENT PRIMARY KEY," +
                    "    patient_type            VARCHAR(20)  NOT NULL," +
                    "    mother_id               INT          NULL," +
                    "    pregnancy_id            INT          NULL," +
                    "    baby_id                 INT          NULL," +
                    "    doctor_id               INT          NOT NULL," +
                    "    referred_by_user_id     INT          NULL," +
                    "    reason                  TEXT         NOT NULL," +
                    "    priority                VARCHAR(20)  NOT NULL DEFAULT 'HIGH_RISK'," +
                    "    status                  VARCHAR(20)  NOT NULL DEFAULT 'DRAFT'," +
                    "    appointment_date        DATE         NULL," +
                    "    appointment_time        VARCHAR(30)  NULL," +
                    "    scheduled_at            DATETIME     NULL," +
                    "    clinical_notes          TEXT         NULL," +
                    "    diagnosis               TEXT         NULL," +
                    "    prescriptions           TEXT         NULL," +
                    "    completed_at            DATETIME     NULL," +
                    "    created_at              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "    updated_at              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            jdbcTemplate.execute(sql);
            log.info("✓ DOCTOR_APPOINTMENT table verified/initialized successfully.");
        } catch (Exception e) {
            log.warn("Could not auto-create DOCTOR_APPOINTMENT table (might already exist): {}", e.getMessage());
        }
    }
}
