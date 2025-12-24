package org.example.coursetrackingautomation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.coursetrackingautomation.entity.Course;
import org.example.coursetrackingautomation.entity.Enrollment;
import org.example.coursetrackingautomation.entity.User;
import org.example.coursetrackingautomation.exception.CourseNotFoundException;
import org.example.coursetrackingautomation.exception.DuplicateEnrollmentException;
import org.example.coursetrackingautomation.exception.QuotaExceededException;
import org.example.coursetrackingautomation.repository.CourseRepository;
import org.example.coursetrackingautomation.repository.EnrollmentRepository;
import org.example.coursetrackingautomation.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enrollment işlemlerini yöneten service sınıfı
 * - Öğrenci derse kayıt işlemleri
 * - Kontenjan kontrolü
 * - Tekrar kayıt kontrolü
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentService {
    
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    
    // Aktif enrollment durumları
    private static final List<String> ACTIVE_ENROLLMENT_STATUSES = 
        Arrays.asList("ACTIVE", "ENROLLED", "REGISTERED");
    
    /**
     * Öğrenciyi derse kaydeder
     * Validasyonlar:
     * 1. Kontenjan dolu mu?
     * 2. Öğrenci dersi zaten aldı mı?
     * 
     * @param studentId Öğrenci ID
     * @param courseId Ders ID
     * @return Oluşturulan enrollment
     * @throws QuotaExceededException Kontenjan doluysa
     * @throws DuplicateEnrollmentException Öğrenci dersi zaten aldıysa
     */
    @Transactional
    public Enrollment enrollStudent(Long studentId, Long courseId) {
        log.info("Enrolling student ID: {} to course ID: {}", studentId, courseId);
        
        // Öğrenci ve ders varlığını kontrol et
        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Öğrenci bulunamadı: " + studentId));
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new CourseNotFoundException("Ders bulunamadı: " + courseId));
        
        // Ders aktif mi kontrol et
        if (!course.isActive()) {
            throw new IllegalArgumentException("Ders aktif değil, kayıt yapılamaz");
        }
        
        // VALIDATION 1: Kontenjan kontrolü
        validateQuota(course);
        
        // VALIDATION 2: Öğrenci dersi zaten aldı mı?
        validateDuplicateEnrollment(student, course);
        
        // Enrollment oluştur
        Enrollment enrollment = Enrollment.builder()
            .student(student)
            .course(course)
            .status("ENROLLED") // Varsayılan durum
            .absenteeismCount(0)
            .enrollmentDate(LocalDateTime.now())
            .build();
        
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        
        log.info("Student ID: {} successfully enrolled to course ID: {}", 
            studentId, courseId);
        
        return savedEnrollment;
    }
    
    /**
     * Kontenjan kontrolü yapar
     * 
     * @param course Kontrol edilecek ders
     * @throws QuotaExceededException Kontenjan doluysa
     */
    @Transactional(readOnly = true)
    public void validateQuota(Course course) {
        // Mevcut aktif kayıt sayısını hesapla
        List<Enrollment> allEnrollments = enrollmentRepository.findAll();
        long currentEnrollments = allEnrollments.stream()
            .filter(e -> e.getCourse().getId().equals(course.getId()))
            .filter(e -> ACTIVE_ENROLLMENT_STATUSES.contains(e.getStatus()))
            .count();
        
        if (currentEnrollments >= course.getQuota()) {
            String message = String.format(
                "Kontenjan dolu! Ders: %s, Mevcut kayıt: %d, Kontenjan: %d",
                course.getCode(), currentEnrollments, course.getQuota());
            log.warn(message);
            throw new QuotaExceededException(message);
        }
        
        log.debug("Quota check passed for course: {}, Current: {}, Quota: {}", 
            course.getCode(), currentEnrollments, course.getQuota());
    }
    
    /**
     * Öğrencinin dersi zaten alıp almadığını kontrol eder
     * 
     * @param student Kontrol edilecek öğrenci
     * @param course Kontrol edilecek ders
     * @throws DuplicateEnrollmentException Öğrenci dersi zaten aldıysa
     */
    @Transactional(readOnly = true)
    public void validateDuplicateEnrollment(User student, Course course) {
        // Öğrencinin bu derse aktif kaydı var mı kontrol et
        List<Enrollment> allEnrollments = enrollmentRepository.findAll();
        boolean exists = allEnrollments.stream()
            .anyMatch(e -> e.getStudent().getId().equals(student.getId()) &&
                          e.getCourse().getId().equals(course.getId()) &&
                          ACTIVE_ENROLLMENT_STATUSES.contains(e.getStatus()));
        
        if (exists) {
            String message = String.format(
                "Öğrenci (%s) bu dersi (%s) zaten almış durumda",
                student.getUsername(), course.getCode());
            log.warn(message);
            throw new DuplicateEnrollmentException(message);
        }
        
        log.debug("Duplicate enrollment check passed for student: {}, course: {}", 
            student.getUsername(), course.getCode());
    }
    
    /**
     * Öğrencinin bir derse kayıtlı olup olmadığını kontrol eder
     * 
     * @param studentId Öğrenci ID
     * @param courseId Ders ID
     * @return true eğer öğrenci derse kayıtlıysa
     */
    @Transactional(readOnly = true)
    public boolean isStudentEnrolled(Long studentId, Long courseId) {
        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Öğrenci bulunamadı: " + studentId));
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new CourseNotFoundException("Ders bulunamadı: " + courseId));
        
        List<Enrollment> allEnrollments = enrollmentRepository.findAll();
        return allEnrollments.stream()
            .anyMatch(e -> e.getStudent().getId().equals(student.getId()) &&
                          e.getCourse().getId().equals(course.getId()) &&
                          ACTIVE_ENROLLMENT_STATUSES.contains(e.getStatus()));
    }
    
    /**
     * Öğrencinin bir derse kaydını iptal eder (DROPPED olarak işaretler)
     * 
     * @param studentId Öğrenci ID
     * @param courseId Ders ID
     */
    @Transactional
    public void dropEnrollment(Long studentId, Long courseId) {
        log.info("Dropping enrollment for student ID: {} from course ID: {}", 
            studentId, courseId);
        
        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Öğrenci bulunamadı: " + studentId));
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new CourseNotFoundException("Ders bulunamadı: " + courseId));
        
        // Öğrencinin bu derse kaydını bul
        List<Enrollment> allEnrollments = enrollmentRepository.findAll();
        Enrollment enrollment = allEnrollments.stream()
            .filter(e -> e.getStudent().getId().equals(studentId) &&
                        e.getCourse().getId().equals(courseId) &&
                        ACTIVE_ENROLLMENT_STATUSES.contains(e.getStatus()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Öğrenci bu derse kayıtlı değil"));
        
        enrollment.setStatus("DROPPED");
        enrollmentRepository.save(enrollment);
        
        log.info("Enrollment dropped successfully for student ID: {} from course ID: {}", 
            studentId, courseId);
    }
    
    /**
     * Belirli bir derse ait tüm aktif kayıtları getirir
     */
    @Transactional(readOnly = true)
    public List<Enrollment> getActiveEnrollmentsByCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new CourseNotFoundException("Ders bulunamadı: " + courseId));
        
        List<Enrollment> allEnrollments = enrollmentRepository.findAll();
        return allEnrollments.stream()
            .filter(e -> e.getCourse().getId().equals(courseId))
            .filter(e -> ACTIVE_ENROLLMENT_STATUSES.contains(e.getStatus()))
            .collect(Collectors.toList());
    }
    
    /**
     * Belirli bir öğrencinin tüm aktif kayıtlarını getirir
     */
    @Transactional(readOnly = true)
    public List<Enrollment> getActiveEnrollmentsByStudent(Long studentId) {
        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Öğrenci bulunamadı: " + studentId));
        
        List<Enrollment> allEnrollments = enrollmentRepository.findAll();
        return allEnrollments.stream()
            .filter(e -> e.getStudent().getId().equals(studentId))
            .filter(e -> ACTIVE_ENROLLMENT_STATUSES.contains(e.getStatus()))
            .collect(Collectors.toList());
    }
}
