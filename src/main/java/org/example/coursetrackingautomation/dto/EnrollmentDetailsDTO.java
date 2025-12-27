package org.example.coursetrackingautomation.dto;

public record EnrollmentDetailsDTO(
    Long id,
    String studentDisplay,
    String courseDisplay,
    String status
) {
}
