package org.example.coursetrackingautomation.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.coursetrackingautomation.dto.CreateUserRequest;
import org.example.coursetrackingautomation.dto.RoleDTO;
import org.example.coursetrackingautomation.entity.*;
import org.example.coursetrackingautomation.repository.*;
import org.example.coursetrackingautomation.service.GradeService;
import org.example.coursetrackingautomation.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
/**
 * Seeds demo/initial data on application startup.
 *
 * <p>This runner is intended to populate local environments with a default admin account,
 * sample instructors/students, courses, enrollments, attendance records, and (optionally)
 * partially/fully graded sample data.</p>
 */
public class DataSeeder implements CommandLineRunner {

    private static final String DEFAULT_PASSWORD = "123";
    private static final String CURRENT_TERM = "2025-2026 Güz";
    private static final String EMAIL_DOMAIN_STUDENT = "ogrenci.universite.local";
    private static final String EMAIL_DOMAIN_INSTRUCTOR = "universite.local";
    private static final EnrollmentStatus STATUS_ACTIVE = EnrollmentStatus.ACTIVE;
    private static final String PREFIX_STUDENT = "ogrenci";
    private static final String PREFIX_INSTRUCTOR = "akademisyen";
    private static final List<String> FULLY_GRADED_COURSES = List.of("BLM101", "MAT101");
    private static final List<String> PARTIALLY_GRADED_COURSES = List.of("BLM102", "BLM201");
    private static final String[] FIRST_NAMES = {"Ali", "Veli", "Ayşe", "Fatma", "Mehmet", "Zeynep", "Can", "Elif", "Murat", "Selin"};
    private static final String[] LAST_NAMES = {"Yılmaz", "Kaya", "Demir", "Şahin", "Çelik", "Aydın", "Arslan", "Doğan", "Koç", "Öztürk"};

    private record CourseSeedData(
        String code,
        String name,
        int credit,
        int quota,
        int weeklyTotalHours,
        int weeklyTheoryHours,
        int weeklyPracticeHours,
        boolean active
    ) {}
    private static final List<CourseSeedData> INITIAL_COURSES = List.of(
        new CourseSeedData("BLM101", "Programlamaya Giriş", 4, 80, 4, 3, 1, true),
        new CourseSeedData("BLM102", "Veri Yapıları", 4, 70, 4, 3, 1, true),
        new CourseSeedData("BLM201", "Veritabanı Sistemleri", 3, 70, 3, 2, 1, true),
        new CourseSeedData("BLM202", "İşletim Sistemleri", 3, 60, 3, 2, 1, false),
        new CourseSeedData("MAT101", "Matematik I", 4, 90, 4, 4, 0, true),
        new CourseSeedData("IST101", "İstatistik", 3, 80, 3, 3, 0, true),
        new CourseSeedData("FIZ101", "Fizik I", 4, 80, 4, 3, 1, true),
        new CourseSeedData("ING101", "İngilizce I", 2, 100, 2, 2, 0, true)
    );

    private final UserService userService;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GradeRepository gradeRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final GradeService gradeService;

    @Override
    @Transactional
    /**
     * Entry point invoked by Spring Boot during application startup.
     *
     * @param args command-line arguments
     */
    public void run(String... args) {
        seedAll();
    }

    /**
     * Performs the full data seeding routine.
     *
     * <p>This method is protected to allow overriding in tests or specialized bootstrapping flows.</p>
     */
    protected void seedAll() {
        log.info("Checking and Seeding Data...");

        userService.ensureDefaultAdminUserExists();

        List<User> instructors = getOrSeedInstructors();
        List<User> students = getOrSeedStudents();
        List<Course> courses = getOrSeedCourses(instructors);

        if (enrollmentRepository.count() == 0) {
            log.info("Seeding Enrollments, Grades, and Attendance...");
            seedEnrollmentsGradesAndAttendance(students, courses);
        } else {
            log.info("Enrollment data already exists. Skipping.");
        }
        
        log.info("Data Seeding Process Completed.");
    }

    private List<User> getOrSeedInstructors() {
        List<User> existing = userRepository.findByRoleAndActiveTrue(Role.INSTRUCTOR);
        if (!existing.isEmpty()) return existing;

        log.info("Seeding instructors...");
        List<User> newInstructors = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            String username = String.format("%s%02d", PREFIX_INSTRUCTOR, i);
            String firstName = getRandomName(FIRST_NAMES, i);
            String lastName = getRandomName(LAST_NAMES, i);
            String email = String.format("%s@%s", username, EMAIL_DOMAIN_INSTRUCTOR);
            String phone = "0500" + String.format("%07d", i);

            User created = userService.createUser(new CreateUserRequest(
                username, DEFAULT_PASSWORD, firstName, lastName, RoleDTO.INSTRUCTOR,
                null, email, phone, true
            ));
            newInstructors.add(created);
        }
        return newInstructors;
    }

    private List<User> getOrSeedStudents() {
        if (userRepository.findFirstByRoleAndActiveTrue(Role.STUDENT).isPresent()) {
            return userRepository.findByRoleAndActiveTrue(Role.STUDENT);
        }

        log.info("Seeding students...");
        List<User> newStudents = new ArrayList<>();
        
        for (int i = 1; i <= 100; i++) {
            String username = String.format("%s%03d", PREFIX_STUDENT, i);
            String firstName = getRandomName(FIRST_NAMES, i);
            String lastName = getRandomName(LAST_NAMES, i);
            String studentNumber = String.format("2025%04d", i);
            String email = String.format("%s@%s", username, EMAIL_DOMAIN_STUDENT);

            User created = userService.createUser(new CreateUserRequest(
                username, DEFAULT_PASSWORD, firstName, lastName, RoleDTO.STUDENT,
                studentNumber, email, null, true
            ));
            newStudents.add(created);
        }
        return newStudents;
    }

    private List<Course> getOrSeedCourses(List<User> instructors) {
        if (courseRepository.count() > 0) {
            List<Course> existing = courseRepository.findAll();

            boolean changed = false;
            for (Course course : existing) {
                Integer weeklyTotalHours = course.getWeeklyTotalHours();
                Integer weeklyTheoryHours = course.getWeeklyTheoryHours();
                Integer weeklyPracticeHours = course.getWeeklyPracticeHours();

                if (weeklyTotalHours == null && weeklyTheoryHours == null && weeklyPracticeHours == null) {
                    course.setWeeklyTotalHours(4);
                    course.setWeeklyTheoryHours(2);
                    course.setWeeklyPracticeHours(2);
                    changed = true;
                    continue;
                }

                if (weeklyTotalHours == null) {
                    int total = (weeklyTheoryHours != null ? weeklyTheoryHours : 0) + (weeklyPracticeHours != null ? weeklyPracticeHours : 0);
                    course.setWeeklyTotalHours(Math.max(total, 0));
                    changed = true;
                    weeklyTotalHours = course.getWeeklyTotalHours();
                }

                if (weeklyTheoryHours == null && weeklyPracticeHours == null) {
                    int total = weeklyTotalHours != null ? weeklyTotalHours : 0;
                    int theory = Math.max(total, 0);
                    course.setWeeklyTheoryHours(theory);
                    course.setWeeklyPracticeHours(0);
                    changed = true;
                    continue;
                }

                if (weeklyTheoryHours == null) {
                    int total = weeklyTotalHours != null ? weeklyTotalHours : 0;
                    int practice = weeklyPracticeHours != null ? weeklyPracticeHours : 0;
                    course.setWeeklyTheoryHours(Math.max(total - practice, 0));
                    changed = true;
                }

                if (weeklyPracticeHours == null) {
                    int total = weeklyTotalHours != null ? weeklyTotalHours : 0;
                    int theory = weeklyTheoryHours != null ? weeklyTheoryHours : 0;
                    course.setWeeklyPracticeHours(Math.max(total - theory, 0));
                    changed = true;
                }
            }

            if (changed) {
                courseRepository.saveAll(existing);
                log.info("Existing courses updated with weekly hours defaults.");
            }

            return existing;
        }

        log.info("Seeding courses...");
        List<Course> newCourses = new ArrayList<>();
        
        for (int i = 0; i < INITIAL_COURSES.size(); i++) {
            CourseSeedData data = INITIAL_COURSES.get(i);
            User instructor = instructors.get(i % instructors.size());

            Course course = Course.builder()
                .code(data.code())
                .name(data.name())
                .credit(data.credit())
                .quota(data.quota())
                .term(CURRENT_TERM)
                .weeklyTotalHours(data.weeklyTotalHours())
                .weeklyTheoryHours(data.weeklyTheoryHours())
                .weeklyPracticeHours(data.weeklyPracticeHours())
                .active(data.active())
                .instructor(instructor)
                .build();

            newCourses.add(courseRepository.save(course));
        }
        return newCourses;
    }

    private void seedEnrollmentsGradesAndAttendance(List<User> students, List<Course> courses) {
        Random random = new Random(20251225L);
        int weeksRecorded = 8; 

        for (User student : students) {
            List<Course> chosenCourses = pickRandomCourses(courses, random);

            for (Course course : chosenCourses) {
                Enrollment enrollment = createEnrollment(student, course);
                int absentCount = generateAttendanceRecords(enrollment, weeksRecorded, random);
                
                enrollment.setAbsenteeismCount(absentCount);
                enrollmentRepository.save(enrollment);

                generateGradesIfApplicable(enrollment, course.getCode(), random);
            }
        }
    }

    private String getRandomName(String[] source, int index) {
        return source[(index - 1) % source.length];
    }

    private List<Course> pickRandomCourses(List<Course> courses, Random random) {
        List<Course> chosen = new ArrayList<>();
        int count = 4 + random.nextInt(3);
        int attempts = 0;
        while (chosen.size() < count && attempts < 20) {
            Course candidate = courses.get(random.nextInt(courses.size()));
            if (!chosen.contains(candidate)) chosen.add(candidate);
            attempts++;
        }
        return chosen;
    }

    private Enrollment createEnrollment(User student, Course course) {
        return enrollmentRepository.save(Enrollment.builder()
            .student(student)
            .course(course)
            .absenteeismCount(0)
            .status(STATUS_ACTIVE)
            .enrollmentDate(LocalDateTime.now().minusMonths(3))
            .build());
    }

    private int generateAttendanceRecords(Enrollment enrollment, int weeks, Random random) {
        int absentWeeksTarget = random.nextInt(5);
        int currentAbsent = 0;

        for (int week = 1; week <= weeks; week++) {
            boolean present = true;
            if (currentAbsent < absentWeeksTarget && random.nextDouble() < 0.3) {
                present = false;
                currentAbsent++;
            }

            attendanceRecordRepository.save(AttendanceRecord.builder()
                .enrollment(enrollment)
                .weekNumber(week)
                .present(present)
                .date(LocalDate.now().minusWeeks(weeks - week))
                .build());
        }
        return currentAbsent;
    }

    private void generateGradesIfApplicable(Enrollment enrollment, String courseCode, Random random) {
        boolean isFullyGraded = FULLY_GRADED_COURSES.contains(courseCode);
        boolean isPartiallyGraded = PARTIALLY_GRADED_COURSES.contains(courseCode);

        if (!isFullyGraded && !isPartiallyGraded) return;

        double midterm = 30 + random.nextInt(71);
        Double finalScore = null;

        if (isFullyGraded) {
            finalScore = (double) (30 + random.nextInt(71));
        } else if (isPartiallyGraded && random.nextDouble() < 0.40) {
            finalScore = (double) (30 + random.nextInt(71));
        }

        Double average = gradeService.calculateAverage(midterm, finalScore);
        String letter = gradeService.determineLetterGrade(average);
        boolean passed = average != null && gradeService.isPassed(letter);

        gradeRepository.save(Grade.builder()
            .enrollment(enrollment)
            .midtermScore(BigDecimal.valueOf(midterm))
            .finalScore(finalScore != null ? BigDecimal.valueOf(finalScore) : null)
            .averageScore(average != null ? BigDecimal.valueOf(average) : null)
            .letterGrade(letter)
            .passed(passed)
            .build());
    }
}