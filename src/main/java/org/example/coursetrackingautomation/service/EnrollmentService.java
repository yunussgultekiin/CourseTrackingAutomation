package org.example.coursetrackingautomation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.coursetrackingautomation.dto.EnrollmentDetailsDTO;
import org.example.coursetrackingautomation.entity.Course;
import org.example.coursetrackingautomation.entity.Enrollment;
import org.example.coursetrackingautomation.entity.EnrollmentStatus;
import org.example.coursetrackingautomation.entity.User;
import org.example.coursetrackingautomation.repository.CourseRepository;
import org.example.coursetrackingautomation.repository.EnrollmentRepository;
import org.example.coursetrackingautomation.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
/**
 * Manages course enrollments.
 *
 * <p>This service enforces basic enrollment rules such as course activity checks, quota validation,
 * duplicate enrollment prevention, and status transitions.</p>
 */
public class EnrollmentService {
    
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    
    private static final List<EnrollmentStatus> ACTIVE_ENROLLMENT_STATUSES = List.of(
        EnrollmentStatus.ACTIVE,
        EnrollmentStatus.ENROLLED,
        EnrollmentStatus.REGISTERED
    );

    private static final EnumSet<EnrollmentStatus> ALLOWED_ENROLLMENT_STATUSES = EnumSet.allOf(EnrollmentStatus.class);
    
    @Transactional
    /**
     * Enrolls a student into a course.
     *
     * <p>The course must be active, the course quota must not be full, and the student must not
     * already have an active enrollment for the same course.</p>
     *
     * @param studentId the student identifier
     * @param courseId the course identifier
     * @return the persisted {@link Enrollment}
     * @throws IllegalArgumentException if validation fails or referenced entities cannot be found
     */
    public Enrollment enrollStudent(Long studentId, Long courseId) {
        log.info("Enrolling student ID: {} to course ID: {}", studentId, courseId);
        
        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Öğrenci bulunamadı: " + studentId));
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Ders bulunamadı: " + courseId));
        
        if (!course.isActive()) {
            throw new IllegalArgumentException("Ders aktif değil; kayıt işlemi yapılamaz");
        }
        
        validateQuota(course);
        
        validateDuplicateEnrollment(student, course);
        
        Enrollment enrollment = Enrollment.builder()
            .student(student)
            .course(course)
            .status(EnrollmentStatus.ENROLLED)
            .absenteeismCount(0)
            .enrollmentDate(LocalDateTime.now())
            .build();
        
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        
        log.info("Student ID: {} successfully enrolled to course ID: {}", 
            studentId, courseId);
        
        return savedEnrollment;
    }
    
    @Transactional(readOnly = true)
    /**
     * Validates that the course has remaining quota.
     *
     * @param course the course to check
     * @throws IllegalArgumentException if the quota is full
     */
    public void validateQuota(Course course) {
        long currentEnrollments = enrollmentRepository.countByCourseIdAndStatusIn(course.getId(), ACTIVE_ENROLLMENT_STATUSES);
        
        if (currentEnrollments >= course.getQuota()) {
            String message = String.format(
                "Kontenjan dolu. Ders: %s, Mevcut kayıt: %d, Kontenjan: %d",
                course.getCode(), currentEnrollments, course.getQuota());
            log.warn(message);
            throw new IllegalArgumentException(message);
        }
        
        log.debug("Quota check passed for course: {}, Current: {}, Quota: {}", 
            course.getCode(), currentEnrollments, course.getQuota());
    }
    
    @Transactional(readOnly = true)
    /**
     * Validates that the student does not already have an active enrollment for the given course.
     *
     * @param student the student entity
     * @param course the course entity
     * @throws IllegalArgumentException if an active enrollment already exists
     */
    public void validateDuplicateEnrollment(User student, Course course) {
        boolean exists = enrollmentRepository.existsByStudentIdAndCourseIdAndStatusIn(
            student.getId(),
            course.getId(),
            ACTIVE_ENROLLMENT_STATUSES
        );
        
        if (exists) {
            String message = String.format(
                "Öğrenci (%s) bu derse (%s) zaten kayıtlı",
                student.getUsername(), course.getCode());
            log.warn(message);
            throw new IllegalArgumentException(message);
        }
        
        log.debug("Duplicate enrollment check passed for student: {}, course: {}", 
            student.getUsername(), course.getCode());
    }
    
    @Transactional(readOnly = true)
    /**
     * Checks whether a student currently has an active enrollment for a given course.
     *
     * @param studentId the student identifier
     * @param courseId the course identifier
     * @return {@code true} if an active enrollment exists
     * @throws IllegalArgumentException if either id is null
     */
    public boolean isStudentEnrolled(Long studentId, Long courseId) {
        if (studentId == null || courseId == null) {
            throw new IllegalArgumentException("Öğrenci id ve ders id boş olamaz");
        }

        return enrollmentRepository.existsByStudentIdAndCourseIdAndStatusIn(studentId, courseId, ACTIVE_ENROLLMENT_STATUSES);
    }
    
    @Transactional
    /**
     * Marks an active enrollment as dropped.
     *
     * @param studentId the student identifier
     * @param courseId the course identifier
     * @throws IllegalArgumentException if no active enrollment exists
     */
    public void dropEnrollment(Long studentId, Long courseId) {
        log.info("Dropping enrollment for student ID: {} from course ID: {}", 
            studentId, courseId);

        Enrollment enrollment = enrollmentRepository
            .findFirstByStudentIdAndCourseIdAndStatusIn(studentId, courseId, ACTIVE_ENROLLMENT_STATUSES)
            .orElseThrow(() -> new IllegalArgumentException("Öğrenci bu derse kayıtlı değil"));
        
        enrollment.setStatus(EnrollmentStatus.DROPPED);
        enrollmentRepository.save(enrollment);
        
        log.info("Enrollment dropped successfully for student ID: {} from course ID: {}", 
            studentId, courseId);
    }

    @Transactional
    /**
     * Updates the status of an enrollment.
     *
     * @param enrollmentId the enrollment identifier
     * @param status new status value; compared case-insensitively and stored as uppercase
     * @return the persisted {@link Enrollment}
     * @throws IllegalArgumentException if validation fails or the enrollment cannot be found
     */
    public Enrollment updateEnrollmentStatus(Long enrollmentId, String status) {
        if (enrollmentId == null) {
            throw new IllegalArgumentException("Kayıt id boş olamaz");
        }
        String safeStatus = status == null ? "" : status.trim().toUpperCase();
        if (safeStatus.isBlank()) {
            throw new IllegalArgumentException("Kayıt durumu boş olamaz");
        }
        EnrollmentStatus newStatus;
        try {
            newStatus = EnrollmentStatus.valueOf(safeStatus);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Desteklenmeyen kayıt durumu: " + safeStatus);
        }
        if (!ALLOWED_ENROLLMENT_STATUSES.contains(newStatus)) {
            throw new IllegalArgumentException("Desteklenmeyen kayıt durumu: " + safeStatus);
        }

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new IllegalArgumentException("Kayıt bulunamadı: " + enrollmentId));

        enrollment.setStatus(newStatus);
        Enrollment saved = enrollmentRepository.save(enrollment);
        log.info("Enrollment status updated: id={}, status={}", saved.getId(), saved.getStatus());
        return saved;
    }
    
    @Transactional(readOnly = true)
    /**
     * Returns all active enrollments for a course.
     *
     * @param courseId the course identifier
     * @return list of active enrollments
     * @throws IllegalArgumentException if {@code courseId} is null
     */
    public List<Enrollment> getActiveEnrollmentsByCourse(Long courseId) {
        if (courseId == null) {
            throw new IllegalArgumentException("Ders id boş olamaz");
        }
        return enrollmentRepository.findByCourseIdAndStatusIn(courseId, ACTIVE_ENROLLMENT_STATUSES);
    }
    
    @Transactional(readOnly = true)
    /**
     * Returns all active enrollments for a student.
     *
     * @param studentId the student identifier
     * @return list of active enrollments
     * @throws IllegalArgumentException if {@code studentId} is null
     */
    public List<Enrollment> getActiveEnrollmentsByStudent(Long studentId) {
        if (studentId == null) {
            throw new IllegalArgumentException("Öğrenci id boş olamaz");
        }
        return enrollmentRepository.findByStudentIdAndStatusIn(studentId, ACTIVE_ENROLLMENT_STATUSES);
    }

    @Transactional(readOnly = true)
    /**
     * Retrieves an enrollment by its identifier.
     *
     * @param enrollmentId the enrollment identifier
     * @return the persisted enrollment
     * @throws IllegalArgumentException if {@code enrollmentId} is null or not found
     */
    public Enrollment getEnrollmentById(Long enrollmentId) {
        if (enrollmentId == null) {
            throw new IllegalArgumentException("Kayıt id boş olamaz");
        }
        return enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new IllegalArgumentException("Kayıt bulunamadı: " + enrollmentId));
    }

    @Transactional(readOnly = true)
    /**
     * Retrieves a UI-friendly enrollment details projection.
     *
     * @param enrollmentId the enrollment identifier
     * @return enrollment details DTO
     * @throws IllegalArgumentException if the enrollment cannot be found
     */
    public EnrollmentDetailsDTO getEnrollmentDetailsById(Long enrollmentId) {
        Enrollment enrollment = getEnrollmentById(enrollmentId);
        String studentDisplay = "-";
        if (enrollment.getStudent() != null) {
            String firstName = enrollment.getStudent().getFirstName() == null ? "" : enrollment.getStudent().getFirstName();
            String lastName = enrollment.getStudent().getLastName() == null ? "" : enrollment.getStudent().getLastName();
            studentDisplay = (firstName + " " + lastName).trim();
            if (studentDisplay.isBlank()) {
                studentDisplay = enrollment.getStudent().getUsername() == null ? "-" : enrollment.getStudent().getUsername();
            }
        }

        String courseDisplay = "-";
        if (enrollment.getCourse() != null) {
            String code = enrollment.getCourse().getCode() == null ? "" : enrollment.getCourse().getCode();
            String name = enrollment.getCourse().getName() == null ? "" : enrollment.getCourse().getName();
            courseDisplay = (code + " - " + name).trim();
            if (courseDisplay.equals("-")) {
                courseDisplay = name.isBlank() ? (code.isBlank() ? "-" : code) : name;
            }
        }

        EnrollmentStatus status = enrollment.getStatus();
        return new EnrollmentDetailsDTO(enrollment.getId(), studentDisplay, courseDisplay, status);
    }
}
