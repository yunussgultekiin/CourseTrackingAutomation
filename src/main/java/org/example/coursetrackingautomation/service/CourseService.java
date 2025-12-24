package org.example.coursetrackingautomation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.coursetrackingautomation.dto.CourseDTO;
import org.example.coursetrackingautomation.entity.Course;
import org.example.coursetrackingautomation.entity.Enrollment;
import org.example.coursetrackingautomation.repository.CourseRepository;
import org.example.coursetrackingautomation.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Course işlemlerini yöneten service sınıfı
 * - Ders oluşturma ve kontenjan belirleme
 * - Soft delete (is_active=false) ve enrollment durumu güncelleme
 * - Entity'den DTO'ya dönüşüm (Mapper işlevselliği dahil)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {
    
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    
    // Aktif enrollment durumları
    private static final List<String> ACTIVE_ENROLLMENT_STATUSES = 
        Arrays.asList("ACTIVE", "ENROLLED", "REGISTERED");
    
    /**
     * Yeni bir ders oluşturur ve kontenjan belirler
     * 
     * @param course Oluşturulacak ders
     * @param quota Kontenjan sayısı (null ise varsayılan değer kullanılır)
     * @return Oluşturulan ders
     */
    @Transactional
    public Course createCourse(Course course, Integer quota) {
        log.info("Creating course with code: {}", course.getCode());
        
        // Kontenjan kontrolü
        if (quota != null && quota <= 0) {
            throw new IllegalArgumentException("Kontenjan 0'dan büyük olmalıdır");
        }
        
        // Kontenjan belirleme
        if (quota != null) {
            course.setQuota(quota);
        } else if (course.getQuota() == null || course.getQuota() <= 0) {
            // Varsayılan kontenjan
            course.setQuota(30);
            log.info("Default quota (30) set for course: {}", course.getCode());
        }
        
        // Ders aktif olarak oluşturulur
        course.setActive(true);
        
        Course savedCourse = courseRepository.save(course);
        log.info("Course created successfully with ID: {} and quota: {}", 
            savedCourse.getId(), savedCourse.getQuota());
        
        return savedCourse;
    }
    
    /**
     * Ders oluşturur (quota parametresi olmadan)
     */
    @Transactional
    public Course createCourse(Course course) {
        return createCourse(course, null);
    }
    
    /**
     * Mevcut bir dersin kontenjanını günceller
     * 
     * @param courseId Ders ID
     * @param newQuota Yeni kontenjan
     * @return Güncellenmiş ders
     */
    @Transactional
    public Course updateQuota(Long courseId, Integer newQuota) {
        log.info("Updating quota for course ID: {} to {}", courseId, newQuota);
        
        if (newQuota == null || newQuota <= 0) {
            throw new IllegalArgumentException("Kontenjan 0'dan büyük olmalıdır");
        }
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Ders bulunamadı: " + courseId));
        
        // Mevcut aktif kayıt sayısını kontrol et
        long currentEnrollments = getCurrentEnrollmentCount(course);
        
        if (newQuota < currentEnrollments) {
            throw new IllegalArgumentException(
                String.format("Yeni kontenjan (%d) mevcut kayıt sayısından (%d) küçük olamaz", 
                    newQuota, currentEnrollments));
        }
        
        course.setQuota(newQuota);
        Course updatedCourse = courseRepository.save(course);
        
        log.info("Quota updated successfully for course ID: {}", courseId);
        return updatedCourse;
    }
    
    /**
     * Dersi soft delete yapar (is_active=false)
     * Dersi alan öğrencilerin kayıt durumunu CANCELLED veya DROPPED olarak günceller
     * 
     * @param courseId Silinecek ders ID
     */
    @Transactional
    public void deactivateCourse(Long courseId) {
        log.info("Deactivating course ID: {}", courseId);
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Ders bulunamadı: " + courseId));
        
        if (!course.isActive()) {
            log.warn("Course ID: {} is already deactivated", courseId);
            return;
        }
        
        // Dersi pasif olarak işaretle
        course.setActive(false);
        courseRepository.save(course);
        
        // Dersi alan öğrencilerin kayıtlarını güncelle
        List<Enrollment> allEnrollments = enrollmentRepository.findAll();
        List<Enrollment> activeEnrollments = allEnrollments.stream()
            .filter(e -> e.getCourse().getId().equals(courseId))
            .filter(e -> ACTIVE_ENROLLMENT_STATUSES.contains(e.getStatus()))
            .collect(Collectors.toList());
        
        int updatedCount = 0;
        for (Enrollment enrollment : activeEnrollments) {
            enrollment.setStatus("CANCELLED");
            updatedCount++;
        }
        
        if (!activeEnrollments.isEmpty()) {
            enrollmentRepository.saveAll(activeEnrollments);
            log.info("Updated {} enrollment(s) to CANCELLED for deactivated course ID: {}", 
                updatedCount, courseId);
        }
        
        log.info("Course ID: {} deactivated successfully", courseId);
    }
    
    /**
     * Ders bilgilerini getirir
     */
    @Transactional(readOnly = true)
    public Course getCourseById(Long courseId) {
        return courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Ders bulunamadı: " + courseId));
    }
    
    /**
     * Ders koduna göre ders getirir
     */
    @Transactional(readOnly = true)
    public Course getCourseByCode(String code) {
        return courseRepository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Ders bulunamadı: " + code));
    }
    
    /**
     * Belirli bir dersin mevcut aktif kayıt sayısını döndürür
     */
    @Transactional(readOnly = true)
    public long getCurrentEnrollmentCount(Course course) {
        List<Enrollment> allEnrollments = enrollmentRepository.findAll();
        return allEnrollments.stream()
            .filter(e -> e.getCourse().getId().equals(course.getId()))
            .filter(e -> ACTIVE_ENROLLMENT_STATUSES.contains(e.getStatus()))
            .count();
    }
    
    /**
     * Belirli bir dersin mevcut aktif kayıt sayısını döndürür (ID ile)
     */
    @Transactional(readOnly = true)
    public long getCurrentEnrollmentCount(Long courseId) {
        Course course = getCourseById(courseId);
        return getCurrentEnrollmentCount(course);
    }
    
    /**
     * Belirli bir dersin kontenjan durumunu kontrol eder
     * 
     * @param courseId Ders ID
     * @return true eğer kontenjan doluysa
     */
    @Transactional(readOnly = true)
    public boolean isQuotaFull(Long courseId) {
        Course course = getCourseById(courseId);
        long currentEnrollments = getCurrentEnrollmentCount(course);
        return currentEnrollments >= course.getQuota();
    }
    
    // ========== DTO DÖNÜŞÜM METODLARI (Mapper işlevselliği) ==========
    
    /**
     * Course entity'sini CourseDTO'ya dönüştürür (Mapper işlevi)
     */
    private CourseDTO toDTO(Course course, Long currentEnrollmentCount) {
        if (course == null) {
            return null;
        }
        
        CourseDTO.CourseDTOBuilder builder = CourseDTO.builder()
            .id(course.getId())
            .code(course.getCode())
            .name(course.getName())
            .credit(course.getCredit())
            .quota(course.getQuota())
            .term(course.getTerm())
            .active(course.isActive());
        
        // Instructor bilgileri
        if (course.getInstructor() != null) {
            builder.instructorId(course.getInstructor().getId())
                   .instructorName(course.getInstructor().getFirstName() + " " + 
                                  course.getInstructor().getLastName());
        }
        
        // İstatistikler
        if (currentEnrollmentCount != null) {
            builder.currentEnrollmentCount(currentEnrollmentCount);
            if (course.getQuota() != null) {
                builder.availableQuota((long) course.getQuota() - currentEnrollmentCount);
            }
        }
        
        return builder.build();
    }
    
    /**
     * Course entity'sini CourseDTO'ya dönüştürür
     */
    @Transactional(readOnly = true)
    public CourseDTO getCourseDTOById(Long courseId) {
        Course course = getCourseById(courseId);
        long currentEnrollments = getCurrentEnrollmentCount(courseId);
        return toDTO(course, currentEnrollments);
    }
    
    /**
     * Course koduna göre CourseDTO getirir
     */
    @Transactional(readOnly = true)
    public CourseDTO getCourseDTOByCode(String code) {
        Course course = getCourseByCode(code);
        long currentEnrollments = getCurrentEnrollmentCount(course.getId());
        return toDTO(course, currentEnrollments);
    }
    
    /**
     * Tüm aktif dersleri CourseDTO listesi olarak getirir
     */
    @Transactional(readOnly = true)
    public List<CourseDTO> getAllActiveCourseDTOs() {
        List<Course> courses = courseRepository.findAll().stream()
            .filter(Course::isActive)
            .collect(Collectors.toList());
        
        return courses.stream()
            .map(course -> {
                long currentEnrollments = getCurrentEnrollmentCount(course);
                return toDTO(course, currentEnrollments);
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Tüm dersleri (aktif ve pasif) CourseDTO listesi olarak getirir
     */
    @Transactional(readOnly = true)
    public List<CourseDTO> getAllCourseDTOs() {
        List<Course> courses = courseRepository.findAll();
        
        return courses.stream()
            .map(course -> {
                long currentEnrollments = getCurrentEnrollmentCount(course);
                return toDTO(course, currentEnrollments);
            })
            .collect(Collectors.toList());
    }
}
