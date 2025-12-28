package org.example.coursetrackingautomation.dto;

import java.time.LocalDateTime;
import org.example.coursetrackingautomation.entity.EnrollmentStatus;

/**
 * Projection used to render enrollment rows in admin management views.
 */
public record AdminEnrollmentRowDTO(
    Long id,
    Long studentId,
    Long courseId,
    String studentName,
    String courseDisplay,
    EnrollmentStatus status,
    LocalDateTime enrollmentDate
) {
}
