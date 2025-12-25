package org.example.coursetrackingautomation.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.coursetrackingautomation.dto.AdminAttendanceRowDTO;
import org.example.coursetrackingautomation.dto.AdminEnrollmentRowDTO;
import org.example.coursetrackingautomation.dto.AdminStatistics;
import org.example.coursetrackingautomation.dto.AdminUserRowDTO;
import org.example.coursetrackingautomation.dto.CourseDTO;
import org.example.coursetrackingautomation.repository.AttendanceRecordRepository;
import org.example.coursetrackingautomation.repository.CourseRepository;
import org.example.coursetrackingautomation.repository.EnrollmentRepository;
import org.example.coursetrackingautomation.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private static final List<String> ACTIVE_ENROLLMENT_STATUSES = List.of("ACTIVE", "ENROLLED", "REGISTERED");

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    @Transactional(readOnly = true)
    public AdminStatistics getStatistics() {
        long totalUsers = userRepository.count();
        long totalCourses = courseRepository.count();
        long activeEnrollments = enrollmentRepository.countByStatusIn(ACTIVE_ENROLLMENT_STATUSES);
        return new AdminStatistics(totalUsers, totalCourses, activeEnrollments);
    }

    @Transactional(readOnly = true)
    public List<AdminUserRowDTO> getAllUserRows() {
        return userRepository.findAll().stream()
            .map(u -> new AdminUserRowDTO(
                u.getId(),
                u.getUsername(),
                u.getFirstName(),
                u.getLastName(),
                u.getRole(),
                u.getEmail()
            ))
            .toList();
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("Kullanıcı id boş olamaz");
        }
        userRepository.deleteById(userId);
    }

    @Transactional(readOnly = true)
    public List<CourseDTO> getAllCourseRows() {
        return courseService.getAllCourseDTOs();
    }

    @Transactional(readOnly = true)
    public List<AdminEnrollmentRowDTO> getAllEnrollmentRows() {
        return enrollmentRepository.findAll().stream()
            .map(e -> {
                String studentName = e.getStudent() == null
                    ? ""
                    : (e.getStudent().getFirstName() + " " + e.getStudent().getLastName());

                String courseDisplay = e.getCourse() == null
                    ? ""
                    : (e.getCourse().getCode() + " - " + e.getCourse().getName());

                Long studentId = e.getStudent() == null ? null : e.getStudent().getId();
                Long courseId = e.getCourse() == null ? null : e.getCourse().getId();

                return new AdminEnrollmentRowDTO(
                    e.getId(),
                    studentId,
                    courseId,
                    studentName,
                    courseDisplay,
                    e.getStatus(),
                    e.getEnrollmentDate()
                );
            })
            .toList();
    }

    @Transactional
    public void dropEnrollment(Long studentId, Long courseId) {
        enrollmentService.dropEnrollment(studentId, courseId);
    }

    @Transactional
    public void enrollStudent(Long studentId, Long courseId) {
        enrollmentService.enrollStudent(studentId, courseId);
    }

    @Transactional(readOnly = true)
    public List<AdminAttendanceRowDTO> getAllAttendanceRows() {
        return attendanceRecordRepository.findAll().stream()
            .map(r -> {
                var enrollment = r.getEnrollment();
                var student = enrollment == null ? null : enrollment.getStudent();
                var course = enrollment == null ? null : enrollment.getCourse();

                Long studentId = student == null ? null : student.getId();
                Long courseId = course == null ? null : course.getId();

                String studentName = student == null ? "" : (student.getFirstName() + " " + student.getLastName());
                String courseDisplay = course == null ? "" : (course.getCode() + " - " + course.getName());

                return new AdminAttendanceRowDTO(
                    r.getId(),
                    studentId,
                    courseId,
                    studentName,
                    courseDisplay,
                    r.getWeekNumber(),
                    r.isPresent(),
                    r.getDate()
                );
            })
            .toList();
    }
}
