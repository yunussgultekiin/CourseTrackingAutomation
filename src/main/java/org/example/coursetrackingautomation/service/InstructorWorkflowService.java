package org.example.coursetrackingautomation.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.coursetrackingautomation.dto.GradeDTO;
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
public class InstructorWorkflowService {

    private static final int FIRST_WEEK_NUMBER = 1;
    private static final int DEFAULT_TERM_WEEKS = 14;

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GradeRepository gradeRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final GradeService gradeService;
    private final AttendanceService attendanceService;

    @Transactional
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

            Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(row.getStudentId(), course.getId())
                .orElseThrow(() -> new IllegalArgumentException("Kayıt bulunamadı"));

            int previousAbsentCount = enrollment.getAbsenteeismCount() == null ? 0 : Math.max(0, enrollment.getAbsenteeismCount());
            int previousAbsentHoursUi = attendanceService.toAbsentHours(course, previousAbsentCount);

            int updatedAbsentCount = previousAbsentCount;

            boolean manualAttendanceHoursOverride = false;
            if (row.getAttendanceCount() != null) {
                int desiredAbsentHours = Math.max(0, row.getAttendanceCount());
                if (desiredAbsentHours != previousAbsentHoursUi) {
                    Integer weekly = course.getWeeklyTotalHours();
                    if (weekly != null && weekly > 0 && (desiredAbsentHours % weekly != 0)) {
                        throw new IllegalArgumentException(
                            "Devamsızlık saati, dersin haftalık toplam saatinin katı olmalıdır (" + weekly + ")"
                        );
                    }
                    updatedAbsentCount = attendanceService.toAbsentCount(course, desiredAbsentHours);
                    manualAttendanceHoursOverride = true;
                }
            }

            Boolean presentInput = row.getPresent();
            if (normalizedWeekNumber != null && presentInput != null) {
                AttendanceRecord existing = attendanceRecordRepository
                    .findByEnrollmentIdAndWeekNumber(enrollment.getId(), normalizedWeekNumber)
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
                        .weekNumber(normalizedWeekNumber)
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
            } else {
                grade.setAverageScore(BigDecimal.valueOf(average));
                grade.setLetterGrade(letter);
                grade.setPassed(passed);
            }

            gradeRepository.save(grade);

            boolean critical = attendanceService.isAttendanceCritical(course, enrollment.getAbsenteeismCount());
            row.setAbsentCritically(critical);
            row.setAverageScore(average);
            row.setLetterGrade(letter);
            if (average == null) {
                row.setStatus("Notlar girilmedi");
            } else {
                row.setStatus(passed ? "Geçti" : "Kaldı");
            }
            row.setAttendanceCount(attendanceService.toAbsentHours(course, updatedAbsentCount));

        }

        log.info("Instructor updates saved for courseCode={}", courseCode);
    }
}
