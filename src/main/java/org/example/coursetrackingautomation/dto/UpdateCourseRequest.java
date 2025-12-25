package org.example.coursetrackingautomation.dto;

public record UpdateCourseRequest(
    String name,
    Integer credit,
    Integer quota,
    String term,
    Boolean active,
    Long instructorId
) {
}
