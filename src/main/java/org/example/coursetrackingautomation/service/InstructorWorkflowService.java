package org.example.coursetrackingautomation.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.coursetrackingautomation.dto.CourseDTO;
import org.example.coursetrackingautomation.dto.GradeDTO;
import org.example.coursetrackingautomation.dto.GradeStatus;
import org.example.coursetrackingautomation.dto.InstructorCourseRosterDTO;
import org.example.coursetrackingautomation.entity.AttendanceRecord;
import org.example.coursetrackingautomation.entity.Course;
import org.example.coursetrackingautomation.entity.Enrollment;
import org.example.coursetrackingautomation.entity.Grade;
import org.example.coursetrackingautomation.repository.AttendanceRecordRepository;
import org.example.coursetrackingautomation.repository.CourseRepository;
import org.example.coursetrackingautomation.repository.EnrollmentRepository;
import org.example.coursetrackingautomation.repository.GradeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
/**
 * Implements instructor-facing workflows.
 *
 * <p>
 * This service assembles course rosters, computes derived grade/attendance
 * indicators for the UI,
 * and persists instructor edits (attendance and grading) back to the database.
 * </p>
 */
public class InstructorWorkflowService {

    private static final int FIRST_WEEK_NUMBER = 1;
    private static final int DEFAULT_TERM_WEEKS = 14;

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GradeRepository gradeRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final GradeService gradeService;
    private final AttendanceService attendanceService;

    @Transactional(readOnly = true)
    /**
     * Lists codes of active courses taught by the given instructor.
     *
     * @param instructorId instructor identifier
     * @return list of active course codes
     * @throws IllegalArgumentException if {@code instructorId} is null
     */
    public List<String> getActiveCourseCodesForInstructor(Long instructorId) {
        if (instructorId == null) {
            throw new IllegalArgumentException("Akademisyen id boş olamaz");
        }
        return courseRepository.findByInstructorIdAndActiveTrue(instructorId).stream()
                .map(Course::getCode)
                .filter(code -> code != null && !code.isBlank())
                .toList();
    }

    @Transactional(readOnly = true)
    /**
     * Builds the roster for a course, including student grade rows and enrollment
     * id mapping.
     *
     * @param courseCode course code
     * @return roster DTO containing course details and per-student rows
     * @throws IllegalArgumentException if the input is invalid or the course cannot
     *                                  be found
     */
    public InstructorCourseRosterDTO getCourseRoster(String courseCode) {
        if (courseCode == null || courseCode.isBlank()) {
            throw new IllegalArgumentException("Ders kodu boş bırakılamaz");
        }
        Course course = courseRepository.findByCode(courseCode)
                .orElseThrow(() -> new IllegalArgumentException("Ders bulunamadı"));

        CourseDTO courseDto = CourseDTO.builder()
                .id(course.getId())
                .code(course.getCode())
                .name(course.getName())
                .credit(course.getCredit())
                .quota(course.getQuota())
                .term(course.getTerm())
                .active(course.isActive())
                .weeklyTotalHours(course.getWeeklyTotalHours())
                .weeklyTheoryHours(course.getWeeklyTheoryHours())
                .weeklyPracticeHours(course.getWeeklyPracticeHours())
                .instructorId(course.getInstructor() == null ? null : course.getInstructor().getId())
                .instructorName(course.getInstructor() == null ? null
                        : (course.getInstructor().getFirstName() + " " + course.getInstructor().getLastName()))
                .build();

        Map<Long, Long> enrollmentIdByStudentId = new HashMap<>();
        List<GradeDTO> rows = enrollmentRepository.findByCourseIdWithStudentAndGrade(course.getId()).stream()
                .map(enrollment -> {
                    if (enrollment.getStudent() != null && enrollment.getStudent().getId() != null) {
                        enrollmentIdByStudentId.put(enrollment.getStudent().getId(), enrollment.getId());
                    }

                    Double midterm = enrollment.getGrade() == null || enrollment.getGrade().getMidtermScore() == null
                            ? null
                            : enrollment.getGrade().getMidtermScore().doubleValue();
                    Double finalScore = enrollment.getGrade() == null || enrollment.getGrade().getFinalScore() == null
                            ? null
                            : enrollment.getGrade().getFinalScore().doubleValue();

                    boolean graded = midterm != null && finalScore != null;
                    Double average = gradeService.calculateAverage(midterm, finalScore);
                    String letter = graded ? gradeService.determineLetterGrade(average) : null;
                    boolean passed = graded && gradeService.isPassed(letter);

                    int absentHoursUi = attendanceService.toAbsentHours(course, enrollment.getAbsenteeismCount());
                    boolean critical = attendanceService.isAttendanceCritical(course, enrollment.getAbsenteeismCount());

                    GradeStatus status;
                    if (!graded) {
                        status = GradeStatus.NOT_GRADED;
                    } else {
                        status = passed ? GradeStatus.PASSED : GradeStatus.FAILED;
                    }

                    Long studentId = enrollment.getStudent() == null ? null : enrollment.getStudent().getId();
                    String studentName = enrollment.getStudent() == null
                            ? "-"
                            : (String
                                    .valueOf(enrollment.getStudent().getFirstName() == null ? ""
                                            : enrollment.getStudent().getFirstName())
                                    + " "
                                    + String.valueOf(enrollment.getStudent().getLastName() == null ? ""
                                            : enrollment.getStudent().getLastName()))
                                    .trim();

                    return new GradeDTO(
                            studentId,
                            studentName,
                            course.getCode(),
                            course.getName(),
                            course.getCredit(),
                            course.getWeeklyTotalHours(),
                            course.getWeeklyTheoryHours(),
                            course.getWeeklyPracticeHours(),
                            midterm,
                            finalScore,
                            average,
                            letter,
                            status,
                            absentHoursUi,
                            critical,
                            true);
                })
                .toList();

        return new InstructorCourseRosterDTO(courseDto, rows, enrollmentIdByStudentId);
    }

    @Transactional(readOnly = true)
    /**
     * Determines the next week number for attendance entry.
     *
     * @param courseId course identifier
     * @return next week number, clamped to the default term week count
     */
    public int getNextWeekNumber(Long courseId) {
        if (courseId == null) {
            return FIRST_WEEK_NUMBER;
        }
        Integer maxWeekNumber = attendanceRecordRepository.findMaxWeekNumberByCourseId(courseId);
        if (maxWeekNumber == null || maxWeekNumber < FIRST_WEEK_NUMBER) {
            return FIRST_WEEK_NUMBER;
        }
        return Math.min(DEFAULT_TERM_WEEKS, maxWeekNumber + 1);
    }

    @Transactional(readOnly = true)
    /**
     * Returns attendance presence values for a set of enrollments in a specific
     * week.
     *
     * @param enrollmentIds enrollment identifiers
     * @param weekNumber    week number
     * @return map of enrollment id to present flag
     */
    public Map<Long, Boolean> getPresentByEnrollmentIdsAndWeekNumber(Collection<Long> enrollmentIds,
            Integer weekNumber) {
        if (enrollmentIds == null || enrollmentIds.isEmpty() || weekNumber == null) {
            return Map.of();
        }
        List<AttendanceRecord> records = attendanceRecordRepository
                .findByEnrollmentIdsAndWeekNumberWithEnrollment(enrollmentIds, weekNumber);
        Map<Long, Boolean> presentByEnrollmentId = new HashMap<>();
        for (AttendanceRecord record : records) {
            if (record.getEnrollment() != null && record.getEnrollment().getId() != null) {
                presentByEnrollmentId.put(record.getEnrollment().getId(), record.isPresent());
            }
        }
        return presentByEnrollmentId;
    }

    @Transactional
    /**
     * Persists instructor updates for a course.
     *
     * <p>
     * Updates can include grade fields (midterm/final), attendance count (derived
     * from hours),
     * and per-week presence. When week-level presence is provided and no manual
     * hours override is
     * applied, absenteeism count is incremented/decremented accordingly.
     * </p>
     *
     * @param courseCode course code
     * @param weekNumber optional week number (1..term weeks); when provided,
     *                   presence updates are saved
     * @param updates    grade/attendance row updates
     * @throws IllegalArgumentException if the course cannot be found or the week
     *                                  number is invalid
     */
    public void saveCourseStudentUpdates(String courseCode, Integer weekNumber, Iterable<GradeDTO> updates) {
        if (courseCode == null || courseCode.isBlank()) {
            throw new IllegalArgumentException("Ders kodu boş bırakılamaz");
        }
        if (updates == null) {
            return;
        }

        Integer normalizedWeekNumber = null;
        if (weekNumber != null) {
            int wk = weekNumber;
            if (wk < FIRST_WEEK_NUMBER || wk > DEFAULT_TERM_WEEKS) {
                throw new IllegalArgumentException("Geçersiz hafta numarası: " + wk);
            }
            normalizedWeekNumber = wk;
        }

        Course course = courseRepository.findByCode(courseCode)
                .orElseThrow(() -> new IllegalArgumentException("Ders bulunamadı"));

        for (GradeDTO row : updates) {
            if (row == null || row.getStudentId() == null) {
                continue;
            }
            processStudentUpdate(course, normalizedWeekNumber, row);
        }

        log.info("Instructor updates saved for courseCode={}", courseCode);
    }

    private void processStudentUpdate(Course course, Integer weekNumber, GradeDTO row) {
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(row.getStudentId(), course.getId())
                .orElseThrow(() -> new IllegalArgumentException("Kayıt bulunamadı"));
        int updatedAbsentCount = updateAttendance(course, enrollment, weekNumber, row);
        updateGrade(enrollment, row);
        boolean critical = attendanceService.isAttendanceCritical(course, updatedAbsentCount);
        row.setAbsentCritically(critical);
        row.setAttendanceCount(attendanceService.toAbsentHours(course, updatedAbsentCount));
    }

    private int updateAttendance(Course course, Enrollment enrollment, Integer weekNumber, GradeDTO row) {
        int previousAbsentCount = enrollment.getAbsenteeismCount() == null ? 0
                : Math.max(0, enrollment.getAbsenteeismCount());
        int previousAbsentHoursUi = attendanceService.toAbsentHours(course, previousAbsentCount);

        int updatedAbsentCount = previousAbsentCount;
        boolean manualAttendanceHoursOverride = false;

        if (row.getAttendanceCount() != null) {
            int desiredAbsentHours = Math.max(0, row.getAttendanceCount());
            if (desiredAbsentHours != previousAbsentHoursUi) {
                Integer weekly = course.getWeeklyTotalHours();
                if (weekly != null && weekly > 0 && (desiredAbsentHours % weekly != 0)) {
                    throw new IllegalArgumentException(
                            "Devamsızlık saati, dersin haftalık toplam saatinin katı olmalıdır (" + weekly + ")");
                }
                updatedAbsentCount = attendanceService.toAbsentCount(course, desiredAbsentHours);
                manualAttendanceHoursOverride = true;
            }
        }

        Boolean presentInput = row.getPresent();
        if (weekNumber != null && presentInput != null) {
            AttendanceRecord existing = attendanceRecordRepository
                    .findByEnrollmentIdAndWeekNumber(enrollment.getId(), weekNumber)
                    .orElse(null);

            if (!manualAttendanceHoursOverride) {
                boolean previousPresent = existing == null || existing.isPresent();
                boolean updatedPresent = presentInput;

                if (previousPresent != updatedPresent) {
                    if (!updatedPresent) {
                        updatedAbsentCount = Math.max(0, updatedAbsentCount + 1);
                    } else {
                        updatedAbsentCount = Math.max(0, updatedAbsentCount - 1);
                    }
                }
            }

            AttendanceRecord toSave = existing == null
                    ? AttendanceRecord.builder()
                            .enrollment(enrollment)
                            .weekNumber(weekNumber)
                            .present(presentInput)
                            .date(LocalDate.now())
                            .build()
                    : existing;

            toSave.setPresent(presentInput);
            toSave.setDate(LocalDate.now());
            attendanceRecordRepository.save(toSave);
        }

        enrollment.setAbsenteeismCount(updatedAbsentCount);
        enrollmentRepository.save(enrollment);
        return updatedAbsentCount;
    }

    private void updateGrade(Enrollment enrollment, GradeDTO row) {
        Grade grade = gradeRepository.findByEnrollmentId(enrollment.getId()).orElse(null);
        if (grade == null) {
            grade = Grade.builder().enrollment(enrollment).passed(false).build();
        }

        if (row.getMidtermScore() != null) {
            grade.setMidtermScore(BigDecimal.valueOf(row.getMidtermScore()));
        }
        if (row.getFinalScore() != null) {
            grade.setFinalScore(BigDecimal.valueOf(row.getFinalScore()));
        }

        Double average = gradeService.calculateAverage(row.getMidtermScore(), row.getFinalScore());
        String letter = gradeService.determineLetterGrade(average);
        boolean passed = gradeService.isPassed(letter);

        if (average == null) {
            grade.setAverageScore(null);
            grade.setLetterGrade(null);
            grade.setPassed(false);

            row.setAverageScore(null);
            row.setLetterGrade(null);
            row.setStatus(GradeStatus.NOT_GRADED);
        } else {
            grade.setAverageScore(BigDecimal.valueOf(average));
            grade.setLetterGrade(letter);
            grade.setPassed(passed);

            row.setAverageScore(average);
            row.setLetterGrade(letter);
            row.setStatus(passed ? GradeStatus.PASSED : GradeStatus.FAILED);
        }

        gradeRepository.save(grade);
    }
}
