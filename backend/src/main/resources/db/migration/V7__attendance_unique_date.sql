ALTER TABLE attendance_records
ADD CONSTRAINT uq_employee_date UNIQUE (employee_id, attendance_date);
