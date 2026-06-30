CREATE TABLE IF NOT EXISTS qr_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL UNIQUE,
    created_by BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_qr_session_creator FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_qr_sessions_session_id ON qr_sessions(session_id);
CREATE INDEX idx_qr_sessions_expires_at ON qr_sessions(expires_at);

CREATE TABLE IF NOT EXISTS attendance_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    session_id VARCHAR(36) NOT NULL,
    attendance_date DATE NOT NULL,
    check_in_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    notes VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attendance_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT uq_employee_session UNIQUE (employee_id, session_id),
    CONSTRAINT uq_employee_date UNIQUE (employee_id, attendance_date)
) ENGINE=InnoDB;

CREATE INDEX idx_attendance_date ON attendance_records(attendance_date);
CREATE INDEX idx_attendance_employee_date ON attendance_records(employee_id, attendance_date);
CREATE INDEX idx_attendance_status ON attendance_records(status);
