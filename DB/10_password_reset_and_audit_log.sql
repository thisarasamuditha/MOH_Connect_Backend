-- =====================================================================
-- 10_password_reset_and_audit_log.sql  (2026-10-16)
-- Password self-service ("forgot password" by emailed link) and the
-- Admin audit-log screen.
--
-- 1. PASSWORD_RESET_TOKEN: one-time reset links. Only a SHA-256 hash of
--    the token is stored, never the token itself.
-- 2. AUDIT_LOG: written by AuditLogInterceptor on every POST/PUT/PATCH/
--    DELETE. The entity already exists; this creates the table only if the
--    live DB does not have it yet, and adds indexes for the Admin filters.
--
-- MUST be run on moh_db BEFORE deploying the backend that contains
-- PasswordResetToken.java; otherwise /auth/forgot-password and
-- /auth/reset-password fail.
-- Safe to run more than once.
-- =====================================================================

CREATE TABLE IF NOT EXISTS PASSWORD_RESET_TOKEN (
    token_id      INT AUTO_INCREMENT PRIMARY KEY,
    user_id       INT          NOT NULL,
    token_hash    CHAR(64)     NOT NULL,                 -- SHA-256 hex of the token sent by email
    expires_at    DATETIME     NOT NULL,
    used_at       DATETIME     NULL,                     -- set when used or replaced by a newer link
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE INDEX uq_password_reset_token_hash (token_hash),
    INDEX idx_password_reset_user (user_id),

    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES USER (user_id)
);

CREATE TABLE IF NOT EXISTS AUDIT_LOG (
    audit_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id          INT          NULL,                  -- NULL for anonymous calls (login, forgot password)
    role             VARCHAR(30)  NULL,
    http_method      VARCHAR(10)  NOT NULL,
    request_path     VARCHAR(512) NOT NULL,
    action           VARCHAR(20)  NOT NULL,              -- CREATE, UPDATE, DELETE
    response_status  INT          NOT NULL,
    client_ip        VARCHAR(64)  NULL,
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_audit_created_at (created_at),
    INDEX idx_audit_user_id (user_id)
);

-- Verify:
-- SHOW CREATE TABLE PASSWORD_RESET_TOKEN;
-- SHOW CREATE TABLE AUDIT_LOG;
-- If AUDIT_LOG already existed, check its column names match the list above
-- (audit_id, user_id, role, http_method, request_path, action,
--  response_status, client_ip, created_at).
