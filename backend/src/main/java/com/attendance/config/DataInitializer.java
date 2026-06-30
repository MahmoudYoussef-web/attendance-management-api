package com.attendance.config;

import com.attendance.entity.*;
import com.attendance.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Slf4j
@Component
@RequiredArgsConstructor
/**
 * Seeds the database with initial data on first run.
 * <p>Creates default admin user (admin@example.com), work schedules (Morning/Evening/General),
 * departments (Engineering, HR, Marketing, Finance), and sample employees.
 * Skips seed if data already exists (idempotent).
 * Layer: config.</p>
 */
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedSchedules();
        seedDepartments();
        seedUsers();
    }

    private void seedAdmin() {
        if (userRepository.findByEmail("admin@example.com").isPresent()) return;
        userRepository.save(User.builder()
                .email("admin@example.com")
                .passwordHash(passwordEncoder.encode("Admin@123"))
                .role(Role.SUPER_ADMIN)
                .enabled(true).locked(false).failedAttempts(0).build());
        log.info("Created admin: admin@example.com / Admin@123");
    }

    private void seedSchedules() {
        if (workScheduleRepository.count() > 0) return;
        workScheduleRepository.save(WorkSchedule.builder().name("Morning Shift").startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(16, 0)).lateAfterMinutes(15).halfDayAfterMinutes(180).build());
        workScheduleRepository.save(WorkSchedule.builder().name("Evening Shift").startTime(LocalTime.of(14, 0)).endTime(LocalTime.of(22, 0)).lateAfterMinutes(15).halfDayAfterMinutes(180).build());
        workScheduleRepository.save(WorkSchedule.builder().name("General Shift").startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(17, 0)).lateAfterMinutes(15).halfDayAfterMinutes(180).build());
    }

    private void seedDepartments() {
        if (departmentRepository.count() > 0) return;
        departmentRepository.save(Department.builder().name("Engineering").description("Software and systems engineering").build());
        departmentRepository.save(Department.builder().name("Human Resources").description("Talent acquisition and people operations").build());
        departmentRepository.save(Department.builder().name("Marketing").description("Brand, campaigns, and growth").build());
        departmentRepository.save(Department.builder().name("Finance").description("Accounting and financial planning").build());
    }

    private void seedUsers() {
        var schedules = workScheduleRepository.findAll();
        if (schedules.isEmpty()) return;

        // ponytail: look up depts by name, avoids index fragility when some exist manually
        trySeedEmployee("ahmed@example.com", "Ahmed", "Hassan", "EMP003", findDept("Engineering"), schedules.get(0), Role.EMPLOYEE, "Senior Developer");
        trySeedEmployee("sara@example.com", "Sara", "Ali", "EMP004", findDept("Marketing"), schedules.get(2), Role.EMPLOYEE, "Marketing Lead");
        trySeedEmployee("mohamed@example.com", "Mohamed", "Youssef", "EMP005", findDept("Finance"), schedules.get(2), Role.EMPLOYEE, "Accountant");
        trySeedEmployee("nour@example.com", "Nour", "Ibrahim", "EMP006", findDept("Human Resources"), schedules.get(2), Role.EMPLOYEE, "HR Coordinator");
        trySeedEmployee("khaled@example.com", "Khaled", "Mahmoud", "EMP007", findDept("Engineering"), schedules.get(1), Role.EMPLOYEE, "Backend Developer");
        trySeedEmployee("hr@example.com", "Hala", "Mostafa", "EMP008", findDept("Human Resources"), schedules.get(2), Role.HR_MANAGER, "HR Manager");
    }

    private Department findDept(String name) {
        return departmentRepository.findAll().stream()
                .filter(d -> d.getName().equals(name))
                .findFirst().orElse(null);
    }

    private void trySeedEmployee(String email, String firstName, String lastName, String code, Department dept, WorkSchedule sched, Role role, String position) {
        if (userRepository.findByEmail(email).isPresent()) return;
        if (employeeRepository.findByEmployeeCode(code).isPresent()) return;
        if (dept == null) { log.warn("Skipping {} — department not found", email); return; }
        var user = userRepository.save(User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("Emp@123"))
                .role(role)
                .enabled(true).locked(false).failedAttempts(0).build());
        employeeRepository.save(Employee.builder()
                .user(user)
                .employeeCode(code)
                .firstName(firstName)
                .lastName(lastName)
                .phone("010" + (long) (Math.random() * 100000000))
                .department(dept)
                .position(position)
                .hireDate(LocalDate.of(2024, 1, 15))
                .status(EmploymentStatus.ACTIVE)
                .schedule(sched)
                .build());
        log.info("Created {}: {} / Emp@123", email, email);
    }
}
