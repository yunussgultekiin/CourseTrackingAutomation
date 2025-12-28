package org.example.coursetrackingautomation.dto;

/**
 * Request payload used when creating a new course.
 *
 * <p>This DTO is typically populated from UI form input and validated/processed in the service
 * layer.
 */
public record CreateCourseRequest(
    String code,
    String name,
    Integer credit,
    Integer quota,
    String term,
    Long instructorId,
    Integer weeklyTotalHours,
    Integer weeklyTheoryHours,
    Integer weeklyPracticeHours
) {
}
