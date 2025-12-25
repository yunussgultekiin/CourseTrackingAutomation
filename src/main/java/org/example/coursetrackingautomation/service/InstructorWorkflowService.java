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

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GradeRepository gradeRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final GradeService gradeService;
    private final AttendanceService attendanceService;

    @Transactional
    public void saveCourseStudentUpdates(String courseCode, Iterable<GradeDTO> updates) {
        if (courseCode == null || courseCode.isBlank()) {
            throw new IllegalArgumentException("Ders kodu boş bırakılamaz");
        }
        if (updates == null) {
            return;
        }

        Course course = courseRepository.findByCode(courseCode)
            .orElseThrow(() -> new IllegalArgumentException("Ders bulunamadı"));

        for (GradeDTO row : updates) {
            if (row == null || row.getStudentId() == null) {
                continue;
            }

            Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(row.getStudentId(), course.getId())
                .orElseThrow(() -> new IllegalArgumentException("Kayıt bulunamadı"));

            int previousAbsentHours = enrollment.getAbsenteeismCount() == null ? 0 : enrollment.getAbsenteeismCount();
            int updatedAbsentHours = previousAbsentHours;

            if (row.getAttendanceCount() != null) {
                updatedAbsentHours = Math.max(0, row.getAttendanceCount());
            }

            Boolean presentInput = row.getPresent();
            if (presentInput != null && row.getAttendanceCount() == null) {
                if (!presentInput) {
                    updatedAbsentHours = previousAbsentHours + 1;
                }

                int nextWeekNumber = attendanceRecordRepository.findFirstByEnrollmentIdOrderByWeekNumberDesc(enrollment.getId())
                    .map(r -> r.getWeekNumber() == null ? FIRST_WEEK_NUMBER : r.getWeekNumber() + 1)
                    .orElse(FIRST_WEEK_NUMBER);

                AttendanceRecord attendanceRecord = AttendanceRecord.builder()
                    .enrollment(enrollment)
                    .weekNumber(nextWeekNumber)
                    .present(presentInput)
                    .date(LocalDate.now())
                    .build();
                attendanceRecordRepository.save(attendanceRecord);
            }

            enrollment.setAbsenteeismCount(updatedAbsentHours);
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

            boolean critical = attendanceService.isAttendanceCritical(enrollment.getAbsenteeismCount() == null ? 0 : enrollment.getAbsenteeismCount());
            row.setAbsentCritically(critical);
            row.setAverageScore(average);
            row.setLetterGrade(letter);
            if (average == null) {
                row.setStatus("Notlar girilmedi");
            } else {
                row.setStatus(passed ? "Geçti" : "Kaldı");
            }
            row.setAttendanceCount(updatedAbsentHours);

            row.setPresent(null);
        }

        log.info("Instructor updates saved for courseCode={}", courseCode);
    }
}
