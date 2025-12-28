package org.example.coursetrackingautomation.repository;

import java.util.List;
import java.util.Optional;

import org.example.coursetrackingautomation.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
/**
 * Spring Data repository for {@link Course} persistence and lookups.
 */
public interface CourseRepository extends JpaRepository<Course, Long> {

    /**
     * Finds a course by its code.
     *
     * @param code course code
     * @return matching course if present
     */
    Optional<Course> findByCode(String code);

    /**
     * Finds a course by its code (case-insensitive).
     *
     * @param code course code
     * @return matching course if present
     */
    Optional<Course> findByCodeIgnoreCase(String code);

    /**
     * Returns active courses taught by a given instructor.
     *
     * @param instructorId instructor identifier
     * @return list of active courses
     */
    List<Course> findByInstructorIdAndActiveTrue(Long instructorId);

    /**
     * Returns all active courses.
     *
     * @return list of active courses
     */
    List<Course> findByActiveTrue();
}