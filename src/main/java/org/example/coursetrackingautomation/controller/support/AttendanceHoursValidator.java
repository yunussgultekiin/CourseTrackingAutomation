package org.example.coursetrackingautomation.controller.support;

import java.util.Optional;
import org.example.coursetrackingautomation.dto.CourseDTO;

public final class AttendanceHoursValidator {

    private AttendanceHoursValidator() {
    }

    public static Optional<String> validate(Integer absentHours, CourseDTO selectedCourse) {
        if (absentHours == null || absentHours < 0) {
            return Optional.of("Devamsızlık saati 0 veya daha büyük olmalıdır.");
        }

        if (selectedCourse == null || selectedCourse.getWeeklyTotalHours() == null) {
            return Optional.empty();
        }

        int weekly = selectedCourse.getWeeklyTotalHours();
        if (weekly <= 0) {
            return Optional.empty();
        }

        if (absentHours % weekly != 0) {
            return Optional.of("Devamsızlık saati, dersin haftalık toplam saatinin katı olmalıdır (" + weekly + ").");
        }

        return Optional.empty();
    }
}
