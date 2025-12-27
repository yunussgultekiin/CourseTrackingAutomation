package org.example.coursetrackingautomation.dto;

public record SessionUser(
    Long id,
    String username,
    String firstName,
    String lastName,
    RoleDTO role
) {

    public String fullName() {
        String safeFirstName = firstName == null ? "" : firstName.trim();
        String safeLastName = lastName == null ? "" : lastName.trim();
        return (safeFirstName + " " + safeLastName).trim();
    }
}
