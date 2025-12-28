package org.example.coursetrackingautomation.dto;

/**
 * Minimal user snapshot stored in the UI session.
 *
 * <p>Designed to avoid carrying sensitive or large user objects through UI controllers.
 */
public record SessionUser(
    Long id,
    String username,
    String firstName,
    String lastName,
    RoleDTO role
) {

    /**
     * Returns a display-friendly full name built from first and last name.
     *
     * <p>Null values are treated as empty strings and extra whitespace is trimmed.
     *
     * @return full name suitable for UI labels
     */
    public String fullName() {
        String safeFirstName = firstName == null ? "" : firstName.trim();
        String safeLastName = lastName == null ? "" : lastName.trim();
        return (safeFirstName + " " + safeLastName).trim();
    }
}
