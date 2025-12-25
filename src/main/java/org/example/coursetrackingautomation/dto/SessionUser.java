package org.example.coursetrackingautomation.dto;

import org.example.coursetrackingautomation.entity.Role;

public record SessionUser(
    Long id,
    String username,
    String firstName,
    String lastName,
    Role role
) {

    public String fullName() {
        String safeFirstName = firstName == null ? "" : firstName.trim();
        String safeLastName = lastName == null ? "" : lastName.trim();
        return (safeFirstName + " " + safeLastName).trim();
    }
}
