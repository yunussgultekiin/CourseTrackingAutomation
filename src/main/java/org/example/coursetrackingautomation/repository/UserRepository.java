package org.example.coursetrackingautomation.repository;

import java.util.Optional;
import java.util.List;

import org.example.coursetrackingautomation.entity.User;
import org.example.coursetrackingautomation.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
/**
 * Spring Data repository for {@link User} persistence and lookups.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by username.
     *
     * @param username username to search
     * @return matching user if present
     */
    Optional<User> findByUsername(String username);

    /**
     * Returns the first active user for a role, if any.
     *
     * @param role role filter
     * @return first active user for the role
     */
    Optional<User> findFirstByRoleAndActiveTrue(Role role);

    /**
     * Returns all active users for a role.
     *
     * @param role role filter
     * @return active users for the role
     */
    List<User> findByRoleAndActiveTrue(Role role);
}