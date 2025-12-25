package org.example.coursetrackingautomation.dto;

import java.time.LocalDate;

public record AdminAttendanceRowDTO(
    Long id,
    Long studentId,
    Long courseId,
    String studentName,
    String courseDisplay,
    Integer weekNumber,
    Boolean present,
    LocalDate date
) {
}
