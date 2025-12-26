package org.example.coursetrackingautomation.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.coursetrackingautomation.dto.GradeDTO;
import org.example.coursetrackingautomation.entity.Enrollment;
import org.example.coursetrackingautomation.entity.Grade;
import org.example.coursetrackingautomation.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TranscriptService {

    private static final int GPA_SCALE = 2;

    private final EnrollmentRepository enrollmentRepository;
    private final GradeService gradeService;
    private final AttendanceService attendanceService;

    @Transactional(readOnly = true)
    public List<GradeDTO> getTranscriptGradesForStudent(Long studentId) {
        if (studentId == null) {
            throw new IllegalArgumentException("Öğrenci id boş olamaz");
        }

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        List<GradeDTO> result = new ArrayList<>();

        for (Enrollment enrollment : enrollments) {
            Grade grade = enrollment.getGrade();

            Double midterm = grade == null || grade.getMidtermScore() == null ? null : grade.getMidtermScore().doubleValue();
            Double finalScore = grade == null || grade.getFinalScore() == null ? null : grade.getFinalScore().doubleValue();

            boolean graded = midterm != null && finalScore != null;

            Double average = gradeService.calculateAverage(midterm, finalScore);
            String letter = gradeService.determineLetterGrade(average);

            String status;
            if (!graded) {
                letter = null;
                status = "Notlar girilmedi";
            } else {
                status = gradeService.isPassed(letter) ? "Geçti" : "Kaldı";
            }

            int absentHoursUi = attendanceService.toAbsentHours(enrollment.getCourse(), enrollment.getAbsenteeismCount());
            boolean critical = attendanceService.isAttendanceCritical(enrollment.getCourse(), enrollment.getAbsenteeismCount());

            result.add(new GradeDTO(
                enrollment.getStudent().getId(),
                enrollment.getStudent().getFirstName() + " " + enrollment.getStudent().getLastName(),
                enrollment.getCourse().getCode(),
                enrollment.getCourse().getName(),
                enrollment.getCourse().getCredit(),
                enrollment.getCourse().getWeeklyTotalHours(),
                enrollment.getCourse().getWeeklyTheoryHours(),
                enrollment.getCourse().getWeeklyPracticeHours(),
                midterm,
                finalScore,
                average,
                letter,
                status,
                absentHoursUi,
                critical,
                null
            ));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public String calculateGpaText(List<GradeDTO> grades) {
        if (grades == null || grades.isEmpty()) {
            return "0.00";
        }

        BigDecimal totalQualityPoints = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        for (GradeDTO grade : grades) {
            if (grade == null) {
                continue;
            }

            String letter = grade.getLetterGrade();
            Integer credit = grade.getCredit();

            if (letter == null || letter.isBlank()) {
                continue;
            }
            if (credit == null || credit <= 0) {
                continue;
            }

            BigDecimal points = letterToPoints(letter);
            if (points == null) {
                continue;
            }

            BigDecimal creditBd = BigDecimal.valueOf(credit);
            totalCredits = totalCredits.add(creditBd);
            totalQualityPoints = totalQualityPoints.add(points.multiply(creditBd));
        }

        if (totalCredits.compareTo(BigDecimal.ZERO) == 0) {
            return "0.00";
        }

        BigDecimal gpa = totalQualityPoints
            .divide(totalCredits, GPA_SCALE, RoundingMode.HALF_UP);

        return gpa.toPlainString();
    }

    private BigDecimal letterToPoints(String letterGrade) {
        if (letterGrade == null) {
            return null;
        }

        return switch (letterGrade.trim().toUpperCase()) {
            case "AA" -> BigDecimal.valueOf(4.00);
            case "BA" -> BigDecimal.valueOf(3.50);
            case "BB" -> BigDecimal.valueOf(3.00);
            case "CB" -> BigDecimal.valueOf(2.50);
            case "CC" -> BigDecimal.valueOf(2.00);
            case "DC" -> BigDecimal.valueOf(1.50);
            case "DD" -> BigDecimal.valueOf(1.00);
            case "FD" -> BigDecimal.valueOf(0.50);
            case "FF" -> BigDecimal.valueOf(0.00);
            default -> null;
        };
    }
}
