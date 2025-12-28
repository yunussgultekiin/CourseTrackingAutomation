package org.example.coursetrackingautomation.entity;

/**
 * Supported roles for {@link User} accounts.
 *
 * <p>The role determines which dashboard a user can access and which operations they are permitted
 * to perform (e.g., administration, instruction, enrollment).
 */
public enum Role {
    ADMIN, INSTRUCTOR, STUDENT
}