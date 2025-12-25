package org.example.coursetrackingautomation.dto;

import java.time.LocalDateTime;

public record AdminEnrollmentRowDTO(
    Long id,
    Long studentId,
    Long courseId,
    String studentName,
    String courseDisplay,
    String status,
    LocalDateTime enrollmentDate
) {
}
