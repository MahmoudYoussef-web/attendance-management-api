CREATE TABLE IF NOT EXISTS employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    employee_code VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    department_id BIGINT,
    position VARCHAR(100),
    hire_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    schedule_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_employee_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_employee_department FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL,
    CONSTRAINT fk_employee_schedule FOREIGN KEY (schedule_id) REFERENCES work_schedules(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE INDEX idx_employees_department_id ON employees(department_id);
CREATE INDEX idx_employees_schedule_id ON employees(schedule_id);
CREATE INDEX idx_employees_status ON employees(status);
