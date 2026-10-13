-- =====================================================================
-- 08_visit_requests.sql  (2026-10-13)
-- Client request 3.1: Mother Portal - Emergency & Special Visit Requests
--
-- Mothers send home-visit / clinic-consultation requests (routine or urgent)
-- to the midwives (PHMs) of their PHM area.
--
-- MUST be run on moh_db BEFORE deploying the backend that contains
-- VisitRequest.java; otherwise the /visit-requests endpoints fail.
-- Safe to run more than once.
-- =====================================================================

CREATE TABLE IF NOT EXISTS VISIT_REQUEST (
    request_id              INT AUTO_INCREMENT PRIMARY KEY,
    mother_id               INT          NOT NULL,
    -- PHM area at the time of the request; routes it to that area's midwives
    phm_area_id             INT          NOT NULL,
    request_type            VARCHAR(30)  NOT NULL,                    -- HOME_VISIT, CLINIC_CONSULTATION
    urgency                 VARCHAR(20)  NOT NULL DEFAULT 'ROUTINE',  -- ROUTINE, URGENT
    reason                  TEXT         NOT NULL,
    preferred_date          DATE         NULL,
    contact_number          VARCHAR(20)  NULL,
    status                  VARCHAR(20)  NOT NULL DEFAULT 'PENDING',  -- PENDING, ACKNOWLEDGED, SCHEDULED, COMPLETED, DECLINED, CANCELLED
    handled_by_midwife_id   INT          NULL,
    scheduled_at            DATETIME     NULL,
    midwife_response        TEXT         NULL,
    acknowledged_at         DATETIME     NULL,
    closed_at               DATETIME     NULL,
    created_at              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_visit_request_area_status (phm_area_id, status),
    INDEX idx_visit_request_mother (mother_id),

    CONSTRAINT fk_visit_request_mother  FOREIGN KEY (mother_id)             REFERENCES MOTHER (mother_id),
    CONSTRAINT fk_visit_request_area    FOREIGN KEY (phm_area_id)           REFERENCES PHM_AREA (phm_area_id),
    CONSTRAINT fk_visit_request_midwife FOREIGN KEY (handled_by_midwife_id) REFERENCES MIDWIFE (midwife_id)
);

-- Verify:
-- SHOW CREATE TABLE VISIT_REQUEST;
