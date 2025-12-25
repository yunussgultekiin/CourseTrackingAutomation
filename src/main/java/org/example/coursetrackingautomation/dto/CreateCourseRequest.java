package org.example.coursetrackingautomation.dto;

public record CreateCourseRequest(
    String code,
    String name,
    Integer credit,
    Integer quota,
    String term,
    Long instructorId
) {
}
