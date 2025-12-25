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

            int absentHours = enrollment.getAbsenteeismCount() == null ? 0 : enrollment.getAbsenteeismCount();
            boolean critical = attendanceService.isAttendanceCritical(absentHours);

            result.add(new GradeDTO(
                enrollment.getStudent().getId(),
                enrollment.getStudent().getFirstName() + " " + enrollment.getStudent().getLastName(),
                enrollment.getCourse().getCode(),
                enrollment.getCourse().getName(),
                enrollment.getCourse().getCredit(),
                midterm,
                finalScore,
                average,
                letter,
                status,
                absentHours,
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

        double sum = 0.0;
        int count = 0;

        for (GradeDTO grade : grades) {
            if (grade == null || grade.getAverageScore() == null) {
                continue;
            }
            sum += grade.getAverageScore();
            count++;
        }

        if (count == 0) {
            return "0.00";
        }

        BigDecimal gpa = BigDecimal.valueOf(sum / count).setScale(GPA_SCALE, RoundingMode.HALF_UP);
        return gpa.toPlainString();
    }
}
