package org.example.coursetrackingautomation.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.coursetrackingautomation.dto.AdminAttendanceRowDTO;
import org.example.coursetrackingautomation.dto.AdminEnrollmentRowDTO;
import org.example.coursetrackingautomation.dto.AdminStatistics;
import org.example.coursetrackingautomation.dto.AdminUserRowDTO;
import org.example.coursetrackingautomation.dto.CourseDTO;
import org.example.coursetrackingautomation.dto.RoleDTO;
import org.example.coursetrackingautomation.entity.EnrollmentStatus;
import org.example.coursetrackingautomation.entity.Enrollment;
import org.example.coursetrackingautomation.repository.AttendanceRecordRepository;
import org.example.coursetrackingautomation.repository.CourseRepository;
import org.example.coursetrackingautomation.repository.EnrollmentRepository;
import org.example.coursetrackingautomation.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
/**
 * Provides read models and administrative operations for the admin dashboard.
 *
 * <p>This service aggregates statistics and returns flattened DTO rows used by JavaFX table views.
 * It also delegates enrollment actions to {@link EnrollmentService} to reuse the core business rules.</p>
 */
public class AdminDashboardService {

    private static final List<EnrollmentStatus> ACTIVE_ENROLLMENT_STATUSES = List.of(
        EnrollmentStatus.ACTIVE,
        EnrollmentStatus.ENROLLED,
        EnrollmentStatus.REGISTERED
    );

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    @Transactional(readOnly = true)
    /**
     * Computes high-level dashboard statistics.
     *
     * @return statistics summary (users, courses, active enrollments)
     */
    public AdminStatistics getStatistics() {
        long totalUsers = userRepository.count();
        long totalCourses = courseRepository.count();
        long activeEnrollments = enrollmentRepository.countByStatusIn(ACTIVE_ENROLLMENT_STATUSES);
        return new AdminStatistics(totalUsers, totalCourses, activeEnrollments);
    }

    @Transactional(readOnly = true)
    /**
     * Returns all users as admin table rows.
     *
     * @return list of user row DTOs
     */
    public List<AdminUserRowDTO> getAllUserRows() {
        return userRepository.findAll().stream()
            .map(u -> new AdminUserRowDTO(
                u.getId(),
                u.getUsername(),
                u.getFirstName(),
                u.getLastName(),
                RoleDTO.valueOf(u.getRole().name()),
                u.getEmail()
            ))
            .toList();
    }

    @Transactional
    /**
     * Deletes a user by id.
     *
     * @param userId the user identifier
     * @throws IllegalArgumentException if {@code userId} is null or the user cannot be found
     */
    public void deleteUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("Kullanıcı id boş olamaz");
        }
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı: " + userId));
        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    /**
     * Returns all courses as admin table rows.
     *
     * @return list of course DTOs
     */
    public List<CourseDTO> getAllCourseRows() {
        return courseService.getAllCourseDTOs();
    }

    @Transactional(readOnly = true)
    /**
     * Returns all enrollments as admin table rows.
     *
     * @return list of enrollment row DTOs
     */
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

    @Transactional(readOnly = true)
    /**
     * Returns enrollment rows filtered at the database level.
     *
     * <p>This method is intended for the admin "Kayıtlar" screen where fetching all enrollments
     * and filtering client-side can be slow for larger datasets.</p>
     *
     * @param studentQuery optional partial student name/username
     * @param courseId optional course id
     * @param status optional enrollment status
     * @return matching enrollment row DTOs
     */
    public List<AdminEnrollmentRowDTO> searchEnrollmentRows(String studentQuery, Long courseId, EnrollmentStatus status) {
        List<Enrollment> enrollments = enrollmentRepository.searchAdminEnrollments(studentQuery, courseId, status);
        return enrollments.stream()
            .map(e -> {
                String studentName = e.getStudent() == null
                    ? ""
                    : (e.getStudent().getFirstName() + " " + e.getStudent().getLastName());

                String courseDisplay = e.getCourse() == null
                    ? ""
                    : (e.getCourse().getCode() + " - " + e.getCourse().getName());

                Long studentId = e.getStudent() == null ? null : e.getStudent().getId();
                Long cId = e.getCourse() == null ? null : e.getCourse().getId();

                return new AdminEnrollmentRowDTO(
                    e.getId(),
                    studentId,
                    cId,
                    studentName,
                    courseDisplay,
                    e.getStatus(),
                    e.getEnrollmentDate()
                );
            })
            .toList();
    }

    @Transactional
    /**
     * Drops an active enrollment for the student/course pair.
     *
     * @param studentId the student identifier
     * @param courseId the course identifier
     */
    public void dropEnrollment(Long studentId, Long courseId) {
        enrollmentService.dropEnrollment(studentId, courseId);
    }

    @Transactional
    /**
     * Enrolls a student into a course.
     *
     * @param studentId the student identifier
     * @param courseId the course identifier
     */
    public void enrollStudent(Long studentId, Long courseId) {
        enrollmentService.enrollStudent(studentId, courseId);
    }

    @Transactional(readOnly = true)
    /**
     * Returns all attendance records as admin table rows.
     *
     * @return list of attendance row DTOs
     */
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
