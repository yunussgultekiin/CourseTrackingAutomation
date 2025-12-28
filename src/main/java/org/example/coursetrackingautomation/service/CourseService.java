package org.example.coursetrackingautomation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.coursetrackingautomation.dto.CreateCourseRequest;
import org.example.coursetrackingautomation.dto.CourseDTO;
import org.example.coursetrackingautomation.dto.UpdateCourseRequest;
import org.example.coursetrackingautomation.entity.Course;
import org.example.coursetrackingautomation.entity.EnrollmentStatus;
import org.example.coursetrackingautomation.entity.Role;
import org.example.coursetrackingautomation.entity.User;
import org.example.coursetrackingautomation.repository.CourseRepository;
import org.example.coursetrackingautomation.repository.EnrollmentRepository;
import org.example.coursetrackingautomation.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
/**
 * Manages {@link Course} lifecycle and course read models for the application.
 *
 * <p>This service encapsulates validation and business rules around course creation, updates,
 * activation/deactivation, quota management, and conversion to {@link CourseDTO} for UI use.</p>
 *
 * <p>Validation failures are reported via {@link IllegalArgumentException} with user-friendly
 * messages suitable for surfacing in the UI.</p>
 */
public class CourseService {
    
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    private static final String DEFAULT_TERM = "N/A";
    private static final int DEFAULT_QUOTA = 30;
    
    private static final List<EnrollmentStatus> ACTIVE_ENROLLMENT_STATUSES = List.of(
        EnrollmentStatus.ACTIVE,
        EnrollmentStatus.ENROLLED,
        EnrollmentStatus.REGISTERED
    );
    
    @Transactional
    /**
     * Creates a new course (or persists updates if the supplied entity is new) and ensures a valid quota.
     *
     * <p>If {@code quota} is provided, it overrides the entity's quota. If neither is provided or the
     * effective quota is invalid, a default quota is applied.</p>
     *
     * @param course the course entity to persist
     * @param quota optional quota override; must be greater than 0 when provided
     * @return the persisted course instance
     * @throws IllegalArgumentException if {@code quota} is provided and not greater than 0
     */
    public Course createCourse(Course course, Integer quota) {
        log.info("Creating course with code: {}", course.getCode());
        
        if (quota != null && quota <= 0) {
            throw new IllegalArgumentException("Kontenjan 0'dan büyük olmalıdır");
        }
        
        if (quota != null) {
            course.setQuota(quota);
        } else if (course.getQuota() == null || course.getQuota() <= 0) {
            course.setQuota(DEFAULT_QUOTA);
            log.info("Default quota (30) set for course: {}", course.getCode());
        }
        
        course.setActive(true);
        
        Course savedCourse = courseRepository.save(course);
        log.info("Course created successfully with ID: {} and quota: {}", 
            savedCourse.getId(), savedCourse.getQuota());
        
        return savedCourse;
    }
    
    @Transactional
    /**
     * Convenience overload of {@link #createCourse(Course, Integer)} without a quota override.
     *
     * @param course the course entity to persist
     * @return the persisted course instance
     */
    public Course createCourse(Course course) {
        return createCourse(course, null);
    }

    @Transactional
    /**
     * Creates a course from a request model, applying validation and instructor assignment rules.
     *
     * <p>If a course with the same code exists but is inactive, it is re-activated and updated.</p>
     *
     * @param request the request payload used to create the course
     * @return the persisted course instance
     * @throws IllegalArgumentException if validation fails, the instructor is invalid, or the code conflicts
     */
    public Course createCourse(CreateCourseRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Ders oluşturma isteği boş olamaz");
        }

        String code = request.code() == null ? "" : request.code().trim();
        String name = request.name() == null ? "" : request.name().trim();
        Integer credit = request.credit();
        Integer quota = request.quota();

        if (code.isBlank()) {
            throw new IllegalArgumentException("Ders kodu boş bırakılamaz");
        }

        code = code.toUpperCase(Locale.ROOT);
        if (name.isBlank()) {
            throw new IllegalArgumentException("Ders adı boş bırakılamaz");
        }
        if (credit == null || credit <= 0) {
            throw new IllegalArgumentException("Kredi 0'dan büyük olmalıdır");
        }

        if (request.instructorId() == null) {
            throw new IllegalArgumentException("Akademisyen seçimi zorunludur");
        }

        Course existingByCode = courseRepository.findByCodeIgnoreCase(code).orElse(null);
        if (existingByCode != null && existingByCode.isActive()) {
            throw new IllegalArgumentException("Bu ders kodu zaten mevcut: " + code);
        }

        Integer weeklyTotalHours = requireNonNegative(request.weeklyTotalHours(), "Haftalık toplam saat");
        Integer weeklyTheoryHours = requireNonNegative(request.weeklyTheoryHours(), "Haftalık teori saati");
        Integer weeklyPracticeHours = requireNonNegative(request.weeklyPracticeHours(), "Haftalık uygulama saati");
        validateWeeklyHours(weeklyTotalHours, weeklyTheoryHours, weeklyPracticeHours);

        User instructor = userRepository.findById(request.instructorId())
            .orElseThrow(() -> new IllegalArgumentException("Akademisyen bulunamadı: " + request.instructorId()));
        if (instructor.getRole() != Role.INSTRUCTOR) {
            throw new IllegalArgumentException("Seçilen kullanıcı akademisyen değil");
        }
        if (!instructor.isActive()) {
            throw new IllegalArgumentException("Seçilen akademisyen aktif değil");
        }

        String term = request.term() == null || request.term().isBlank() ? DEFAULT_TERM : request.term().trim();

        if (existingByCode != null) {
            existingByCode.setName(name);
            existingByCode.setCredit(credit);
            existingByCode.setTerm(term);
            existingByCode.setWeeklyTotalHours(weeklyTotalHours);
            existingByCode.setWeeklyTheoryHours(weeklyTheoryHours);
            existingByCode.setWeeklyPracticeHours(weeklyPracticeHours);
            existingByCode.setInstructor(instructor);

            if (quota != null) {
                if (quota <= 0) {
                    throw new IllegalArgumentException("Kontenjan 0'dan büyük olmalıdır");
                }
                existingByCode.setQuota(quota);
            } else if (existingByCode.getQuota() == null || existingByCode.getQuota() <= 0) {
                existingByCode.setQuota(DEFAULT_QUOTA);
            }

            existingByCode.setActive(true);
            Course saved = courseRepository.save(existingByCode);
            log.info("Course re-activated: id={}, code={}", saved.getId(), saved.getCode());
            return saved;
        }

        Course course = Course.builder()
            .code(code)
            .name(name)
            .credit(credit)
            .term(term)
            .weeklyTotalHours(weeklyTotalHours)
            .weeklyTheoryHours(weeklyTheoryHours)
            .weeklyPracticeHours(weeklyPracticeHours)
            .instructor(instructor)
            .build();

        return createCourse(course, quota);
    }
    
    @Transactional
    /**
     * Updates course quota, ensuring the quota remains greater than or equal to current enrollments.
     *
     * @param courseId the course identifier
     * @param newQuota the new quota; must be greater than 0 and not lower than current enrollments
     * @return the persisted course instance
     * @throws IllegalArgumentException if inputs are invalid or the course cannot be found
     */
    public Course updateQuota(Long courseId, Integer newQuota) {
        log.info("Updating quota for course ID: {} to {}", courseId, newQuota);
        
        if (newQuota == null || newQuota <= 0) {
            throw new IllegalArgumentException("Kontenjan 0'dan büyük olmalıdır");
        }
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Ders bulunamadı: " + courseId));
        
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

    @Transactional
    /**
     * Updates mutable course attributes.
     *
     * <p>This method performs partial updates: only non-null (and non-blank where applicable) values
     * from {@code request} are applied. If {@code active=false} is provided, the course is deactivated.
     * When updating quota, the value must not be lower than current enrollments.</p>
     *
     * @param courseId the course identifier
     * @param request the update request
     * @return the updated course instance
     * @throws IllegalArgumentException if validation fails or the course/instructor cannot be found
     */
    public Course updateCourse(Long courseId, UpdateCourseRequest request) {
        if (courseId == null) {
            throw new IllegalArgumentException("Ders id boş olamaz");
        }
        if (request == null) {
            throw new IllegalArgumentException("Güncelleme isteği boş olamaz");
        }

        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Ders bulunamadı: " + courseId));

        if (request.name() != null && !request.name().trim().isBlank()) {
            course.setName(request.name().trim());
        }

        if (request.credit() != null) {
            if (request.credit() <= 0) {
                throw new IllegalArgumentException("Kredi 0'dan büyük olmalıdır");
            }
            course.setCredit(request.credit());
        }

        if (request.term() != null && !request.term().trim().isBlank()) {
            course.setTerm(request.term().trim());
        }

        if (request.weeklyTotalHours() != null || request.weeklyTheoryHours() != null || request.weeklyPracticeHours() != null) {
            Integer mergedTotal = request.weeklyTotalHours() != null ? request.weeklyTotalHours() : course.getWeeklyTotalHours();
            Integer mergedTheory = request.weeklyTheoryHours() != null ? request.weeklyTheoryHours() : course.getWeeklyTheoryHours();
            Integer mergedPractice = request.weeklyPracticeHours() != null ? request.weeklyPracticeHours() : course.getWeeklyPracticeHours();

            mergedTotal = requireNonNegative(mergedTotal, "Haftalık toplam saat");
            mergedTheory = requireNonNegative(mergedTheory, "Haftalık teori saati");
            mergedPractice = requireNonNegative(mergedPractice, "Haftalık uygulama saati");
            validateWeeklyHours(mergedTotal, mergedTheory, mergedPractice);

            course.setWeeklyTotalHours(mergedTotal);
            course.setWeeklyTheoryHours(mergedTheory);
            course.setWeeklyPracticeHours(mergedPractice);
        }

        if (request.quota() != null) {
            Integer newQuota = request.quota();
            if (newQuota <= 0) {
                throw new IllegalArgumentException("Kontenjan 0'dan büyük olmalıdır");
            }

            long currentEnrollments = getCurrentEnrollmentCount(course);
            if (newQuota < currentEnrollments) {
                throw new IllegalArgumentException(
                    String.format("Yeni kontenjan (%d) mevcut kayıt sayısından (%d) küçük olamaz", newQuota, currentEnrollments)
                );
            }
            course.setQuota(newQuota);
        }

        if (request.active() != null) {
            if (!request.active()) {
                deactivateCourse(courseId);
                return courseRepository.findById(courseId)
                    .orElseThrow(() -> new IllegalArgumentException("Ders bulunamadı: " + courseId));
            }
            course.setActive(true);
        }

        if (request.instructorId() != null) {
            User instructor = userRepository.findById(request.instructorId())
                .orElseThrow(() -> new IllegalArgumentException("Akademisyen bulunamadı: " + request.instructorId()));
            if (instructor.getRole() != Role.INSTRUCTOR) {
                throw new IllegalArgumentException("Seçilen kullanıcı akademisyen değil");
            }
            if (!instructor.isActive()) {
                throw new IllegalArgumentException("Seçilen akademisyen aktif değil");
            }
            course.setInstructor(instructor);
        }

        Course saved = courseRepository.save(course);
        log.info("Course updated: id={}, code={}", saved.getId(), saved.getCode());
        return saved;
    }
    
    @Transactional
    /**
     * Deactivates a course.
     *
     * <p>This operation is idempotent. If the course is already inactive, it returns without error.</p>
     *
     * @param courseId the course identifier
     * @throws IllegalArgumentException if the course cannot be found
     */
    public void deactivateCourse(Long courseId) {
        log.info("Deactivating course ID: {}", courseId);
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Ders bulunamadı: " + courseId));
        
        if (!course.isActive()) {
            log.warn("Course ID: {} is already deactivated", courseId);
            return;
        }
        
        course.setActive(false);
        courseRepository.save(course);

        log.info("Course ID: {} deactivated successfully", courseId);
    }
    
    @Transactional(readOnly = true)
    /**
     * Retrieves a course by its identifier.
     *
     * @param courseId the course identifier
     * @return the persisted course
     * @throws IllegalArgumentException if the course cannot be found
     */
    public Course getCourseById(Long courseId) {
        return courseRepository.findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Ders bulunamadı: " + courseId));
    }

    @Transactional(readOnly = true)
    /**
     * Retrieves a {@link CourseDTO} for a course id, enriched with current enrollment counts.
     *
     * @param courseId the course identifier
     * @return the mapped course DTO
     * @throws IllegalArgumentException if the course cannot be found
     */
    public CourseDTO getCourseDTOById(Long courseId) {
        Course course = getCourseById(courseId);
        return toDTO(course, getCurrentEnrollmentCount(course));
    }
    
    @Transactional(readOnly = true)
    /**
     * Retrieves a course by its code.
     *
     * @param code the course code
     * @return the persisted course
     * @throws IllegalArgumentException if the course cannot be found
     */
    public Course getCourseByCode(String code) {
        return courseRepository.findByCode(code)
            .orElseThrow(() -> new IllegalArgumentException("Ders bulunamadı: " + code));
    }
    
    @Transactional(readOnly = true)
    /**
     * Computes the number of active enrollments for a given course entity.
     *
     * @param course the course entity; may be {@code null}
     * @return active enrollment count, or 0 if {@code course} or its id is null
     */
    public long getCurrentEnrollmentCount(Course course) {
        if (course == null || course.getId() == null) {
            return 0L;
        }
        return enrollmentRepository.countByCourseIdAndStatusIn(course.getId(), ACTIVE_ENROLLMENT_STATUSES);
    }
    
    @Transactional(readOnly = true)
    /**
     * Computes the number of active enrollments for a given course id.
     *
     * @param courseId the course identifier
     * @return active enrollment count
     * @throws IllegalArgumentException if the course cannot be found
     */
    public long getCurrentEnrollmentCount(Long courseId) {
        Course course = getCourseById(courseId);
        return getCurrentEnrollmentCount(course);
    }
    
    @Transactional(readOnly = true)
    /**
     * Indicates whether the course quota has been reached or exceeded.
     *
     * @param courseId the course identifier
     * @return {@code true} if current enrollments are greater than or equal to quota
     * @throws IllegalArgumentException if the course cannot be found
     */
    public boolean isQuotaFull(Long courseId) {
        Course course = getCourseById(courseId);
        long currentEnrollments = getCurrentEnrollmentCount(course);
        return currentEnrollments >= course.getQuota();
    }
    
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
            .active(course.isActive())
            .weeklyTotalHours(course.getWeeklyTotalHours())
            .weeklyTheoryHours(course.getWeeklyTheoryHours())
            .weeklyPracticeHours(course.getWeeklyPracticeHours());
        
        if (course.getInstructor() != null) {
            builder.instructorId(course.getInstructor().getId())
                   .instructorName(course.getInstructor().getFirstName() + " " + 
                                  course.getInstructor().getLastName());
        }
        
        if (currentEnrollmentCount != null) {
            builder.currentEnrollmentCount(currentEnrollmentCount);
            if (course.getQuota() != null) {
                builder.availableQuota((long) course.getQuota() - currentEnrollmentCount);
            }
        }
        
        return builder.build();
    }

    private static Integer requireNonNegative(Integer value, String label) {
        if (value == null) {
            throw new IllegalArgumentException(label + " boş bırakılamaz");
        }
        if (value < 0) {
            throw new IllegalArgumentException(label + " 0 veya daha büyük olmalıdır");
        }
        return value;
    }

    private static void validateWeeklyHours(Integer total, Integer theory, Integer practice) {
        if (total == null || theory == null || practice == null) {
            throw new IllegalArgumentException("Haftalık ders saatleri boş bırakılamaz");
        }
        if (total <= 0) {
            throw new IllegalArgumentException("Haftalık toplam saat 0'dan büyük olmalıdır");
        }
        if ((theory + practice) != total) {
            throw new IllegalArgumentException("Haftalık toplam saat, teori + uygulama toplamına eşit olmalıdır");
        }
    }
    @Transactional(readOnly = true)
    /**
     * Retrieves a {@link CourseDTO} by course code, enriched with current enrollment counts.
     *
     * @param code the course code
     * @return the mapped course DTO
     * @throws IllegalArgumentException if the course cannot be found
     */
    public CourseDTO getCourseDTOByCode(String code) {
        Course course = getCourseByCode(code);
        long currentEnrollments = getCurrentEnrollmentCount(course.getId());
        return toDTO(course, currentEnrollments);
    }
    
    @Transactional(readOnly = true)
    /**
     * Lists all active courses as DTOs.
     *
     * @return active course DTOs
     */
    public List<CourseDTO> getAllActiveCourseDTOs() {
        List<Course> courses = courseRepository.findByActiveTrue();
        
        return courses.stream()
            .map(course -> {
                long currentEnrollments = getCurrentEnrollmentCount(course);
                return toDTO(course, currentEnrollments);
            })
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    /**
     * Lists all courses (active and inactive) as DTOs.
     *
     * @return all course DTOs
     */
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
