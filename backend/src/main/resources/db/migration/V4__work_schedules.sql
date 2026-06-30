CREATE TABLE IF NOT EXISTS work_schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    late_after_minutes INT NOT NULL DEFAULT 15,
    half_day_after_minutes INT NOT NULL DEFAULT 180,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;
