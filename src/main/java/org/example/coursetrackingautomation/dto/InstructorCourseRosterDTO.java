package org.example.coursetrackingautomation.dto;

import java.util.List;
import java.util.Map;

public record InstructorCourseRosterDTO(
    CourseDTO course,
    List<GradeDTO> rows,
    Map<Long, Long> enrollmentIdByStudentId
) {
}
