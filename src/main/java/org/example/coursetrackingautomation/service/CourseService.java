package org.example.coursetrackingautomation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.coursetrackingautomation.dto.CreateCourseRequest;
import org.example.coursetrackingautomation.dto.CourseDTO;
import org.example.coursetrackingautomation.dto.UpdateCourseRequest;
import org.example.coursetrackingautomation.entity.Course;
import org.example.coursetrackingautomation.entity.Enrollment;
import org.example.coursetrackingautomation.entity.Role;
import org.example.coursetrackingautomation.entity.User;
import org.example.coursetrackingautomation.repository.CourseRepository;
import org.example.coursetrackingautomation.repository.EnrollmentRepository;
import org.example.coursetrackingautomation.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {
    
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    private static final String DEFAULT_TERM = "N/A";
    
    private static final List<String> ACTIVE_ENROLLMENT_STATUSES = List.of("ACTIVE", "ENROLLED", "REGISTERED");
    
    @Transactional
    public Course createCourse(Course course, Integer quota) {
        log.info("Creating course with code: {}", course.getCode());
        
        if (quota != null && quota <= 0) {
            throw new IllegalArgumentException("Kontenjan 0'dan büyük olmalıdır");
        }
        
        if (quota != null) {
            course.setQuota(quota);
        } else if (course.getQuota() == null || course.getQuota() <= 0) {
            course.setQuota(30);
            log.info("Default quota (30) set for course: {}", course.getCode());
        }
        
        course.setActive(true);
        
        Course savedCourse = courseRepository.save(course);
        log.info("Course created successfully with ID: {} and quota: {}", 
            savedCourse.getId(), savedCourse.getQuota());
        
        return savedCourse;
    }
    
    @Transactional
    public Course createCourse(Course course) {
        return createCourse(course, null);
    }

    @Transactional
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
        if (name.isBlank()) {
            throw new IllegalArgumentException("Ders adı boş bırakılamaz");
        }
        if (credit == null || credit <= 0) {
            throw new IllegalArgumentException("Kredi 0'dan büyük olmalıdır");
        }

        if (request.instructorId() == null) {
            throw new IllegalArgumentException("Akademisyen seçimi zorunludur");
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

        Course course = Course.builder()
            .code(code)
            .name(name)
            .credit(credit)
            .term(request.term() == null || request.term().isBlank() ? DEFAULT_TERM : request.term().trim())
            .weeklyTotalHours(weeklyTotalHours)
            .weeklyTheoryHours(weeklyTheoryHours)
            .weeklyPracticeHours(weeklyPracticeHours)
            .instructor(instructor)
            .build();

        return createCourse(course, quota);
    }
    
    @Transactional
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
    public Course getCourseById(Long courseId) {
        return courseRepository.findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Ders bulunamadı: " + courseId));
    }
    
    @Transactional(readOnly = true)
    public Course getCourseByCode(String code) {
        return courseRepository.findByCode(code)
            .orElseThrow(() -> new IllegalArgumentException("Ders bulunamadı: " + code));
    }
    
    @Transactional(readOnly = true)
    public long getCurrentEnrollmentCount(Course course) {
        if (course == null || course.getId() == null) {
            return 0L;
        }
        return enrollmentRepository.countByCourseIdAndStatusIn(course.getId(), ACTIVE_ENROLLMENT_STATUSES);
    }
    
    @Transactional(readOnly = true)
    public long getCurrentEnrollmentCount(Long courseId) {
        Course course = getCourseById(courseId);
        return getCurrentEnrollmentCount(course);
    }
    
    @Transactional(readOnly = true)
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
    public CourseDTO getCourseDTOById(Long courseId) {
        Course course = getCourseById(courseId);
        long currentEnrollments = getCurrentEnrollmentCount(courseId);
        return toDTO(course, currentEnrollments);
    }
    
    @Transactional(readOnly = true)
    public CourseDTO getCourseDTOByCode(String code) {
        Course course = getCourseByCode(code);
        long currentEnrollments = getCurrentEnrollmentCount(course.getId());
        return toDTO(course, currentEnrollments);
    }
    
    @Transactional(readOnly = true)
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
