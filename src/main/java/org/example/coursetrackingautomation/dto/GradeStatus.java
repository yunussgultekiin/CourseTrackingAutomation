package org.example.coursetrackingautomation.dto;

/**
 * Derived status for an instructor grade row.
 *
 * <p>This is not persisted; it is computed from grade values.
 * The UI should map these values to Turkish text.</p>
 */
public enum GradeStatus {
    NOT_GRADED,
    PASSED,
    FAILED
}
