package org.example.coursetrackingautomation.entity;

/**
 * Domain status of an {@link Enrollment}.
 *
 * <p>Values are persisted as strings via {@code @Enumerated(EnumType.STRING)}.
 * Keep names stable to preserve database compatibility.</p>
 */
public enum EnrollmentStatus {
    ACTIVE,
    ENROLLED,
    REGISTERED,
    DROPPED,
    CANCELLED
}
