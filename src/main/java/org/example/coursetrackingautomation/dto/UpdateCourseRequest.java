package org.example.coursetrackingautomation.dto;

/**
 * Request payload used when updating an existing course.
 *
 * <p>Fields are treated as the desired new values for the course. Validation and update semantics
 * are handled in the service layer.
 */
public record UpdateCourseRequest(
    String name,
    Integer credit,
    Integer quota,
    String term,
    Boolean active,
    Long instructorId,
    Integer weeklyTotalHours,
    Integer weeklyTheoryHours,
    Integer weeklyPracticeHours
) {
}
