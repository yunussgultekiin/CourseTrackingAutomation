package org.example.coursetrackingautomation.dto;

/**
 * Role enum used in DTOs and UI bindings.
 *
 * <p>Mirrors the domain {@code Role} enum while keeping DTO packages decoupled from JPA entities.
 */
public enum RoleDTO {
    ADMIN,
    INSTRUCTOR,
    STUDENT
}
