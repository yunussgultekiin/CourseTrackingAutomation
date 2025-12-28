package org.example.coursetrackingautomation.dto;

import org.example.coursetrackingautomation.entity.EnrollmentStatus;

/**
 * Lightweight enrollment projection used by edit/enrollment management screens.
 *
 * <p>Includes display-ready student and course strings plus the current enrollment status.
 */
public record EnrollmentDetailsDTO(
    Long id,
    String studentDisplay,
    String courseDisplay,
    EnrollmentStatus status
) {
}
