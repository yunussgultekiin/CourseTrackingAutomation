package org.example.coursetrackingautomation.repository;

import java.util.Optional;
import java.util.List;

import org.example.coursetrackingautomation.entity.User;
import org.example.coursetrackingautomation.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findFirstByRoleAndActiveTrue(Role role);

    List<User> findByRoleAndActiveTrue(Role role);
}