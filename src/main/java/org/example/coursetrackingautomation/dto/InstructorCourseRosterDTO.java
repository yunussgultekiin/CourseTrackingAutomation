package org.example.coursetrackingautomation.dto;

import java.util.List;
import java.util.Map;

/**
 * Composite payload used by instructor workflows.
 *
 * <p>Contains the course being managed, the roster rows displayed in the UI, and a lookup map to
 * resolve enrollment ids by student id for update operations.
 */
public record InstructorCourseRosterDTO(
    CourseDTO course,
    List<GradeDTO> rows,
    Map<Long, Long> enrollmentIdByStudentId
) {
}
