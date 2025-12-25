package org.example.coursetrackingautomation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.coursetrackingautomation.entity.Course;
import org.example.coursetrackingautomation.entity.Enrollment;
import org.example.coursetrackingautomation.entity.User;
import org.example.coursetrackingautomation.repository.CourseRepository;
import org.example.coursetrackingautomation.repository.EnrollmentRepository;
import org.example.coursetrackingautomation.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentService {
    
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    
    private static final List<String> ACTIVE_ENROLLMENT_STATUSES = List.of("ACTIVE", "ENROLLED", "REGISTERED");

    private static final Set<String> ALLOWED_ENROLLMENT_STATUSES = Set.of(
        "ACTIVE",
        "ENROLLED",
        "REGISTERED",
        "DROPPED",
        "CANCELLED"
    );
    
    @Transactional
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
            .status("ENROLLED")
            .absenteeismCount(0)
            .enrollmentDate(LocalDateTime.now())
            .build();
        
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        
        log.info("Student ID: {} successfully enrolled to course ID: {}", 
            studentId, courseId);
        
        return savedEnrollment;
    }
    
    @Transactional(readOnly = true)
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
    public boolean isStudentEnrolled(Long studentId, Long courseId) {
        if (studentId == null || courseId == null) {
            throw new IllegalArgumentException("Öğrenci id ve ders id boş olamaz");
        }

        return enrollmentRepository.existsByStudentIdAndCourseIdAndStatusIn(studentId, courseId, ACTIVE_ENROLLMENT_STATUSES);
    }
    
    @Transactional
    public void dropEnrollment(Long studentId, Long courseId) {
        log.info("Dropping enrollment for student ID: {} from course ID: {}", 
            studentId, courseId);

        Enrollment enrollment = enrollmentRepository
            .findFirstByStudentIdAndCourseIdAndStatusIn(studentId, courseId, ACTIVE_ENROLLMENT_STATUSES)
            .orElseThrow(() -> new IllegalArgumentException("Öğrenci bu derse kayıtlı değil"));
        
        enrollment.setStatus("DROPPED");
        enrollmentRepository.save(enrollment);
        
        log.info("Enrollment dropped successfully for student ID: {} from course ID: {}", 
            studentId, courseId);
    }

    @Transactional
    public Enrollment updateEnrollmentStatus(Long enrollmentId, String status) {
        if (enrollmentId == null) {
            throw new IllegalArgumentException("Kayıt id boş olamaz");
        }
        String safeStatus = status == null ? "" : status.trim().toUpperCase();
        if (safeStatus.isBlank()) {
            throw new IllegalArgumentException("Kayıt durumu boş olamaz");
        }
        if (!ALLOWED_ENROLLMENT_STATUSES.contains(safeStatus)) {
            throw new IllegalArgumentException("Desteklenmeyen kayıt durumu: " + safeStatus);
        }

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new IllegalArgumentException("Kayıt bulunamadı: " + enrollmentId));

        enrollment.setStatus(safeStatus);
        Enrollment saved = enrollmentRepository.save(enrollment);
        log.info("Enrollment status updated: id={}, status={}", saved.getId(), saved.getStatus());
        return saved;
    }
    
    @Transactional(readOnly = true)
    public List<Enrollment> getActiveEnrollmentsByCourse(Long courseId) {
        if (courseId == null) {
            throw new IllegalArgumentException("Ders id boş olamaz");
        }
        return enrollmentRepository.findByCourseIdAndStatusIn(courseId, ACTIVE_ENROLLMENT_STATUSES);
    }
    
    @Transactional(readOnly = true)
    public List<Enrollment> getActiveEnrollmentsByStudent(Long studentId) {
        if (studentId == null) {
            throw new IllegalArgumentException("Öğrenci id boş olamaz");
        }
        return enrollmentRepository.findByStudentIdAndStatusIn(studentId, ACTIVE_ENROLLMENT_STATUSES);
    }

    @Transactional(readOnly = true)
    public Enrollment getEnrollmentById(Long enrollmentId) {
        if (enrollmentId == null) {
            throw new IllegalArgumentException("Kayıt id boş olamaz");
        }
        return enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new IllegalArgumentException("Kayıt bulunamadı: " + enrollmentId));
    }
}
