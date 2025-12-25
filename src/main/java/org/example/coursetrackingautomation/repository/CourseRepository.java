package org.example.coursetrackingautomation.repository;

import java.util.List;
import java.util.Optional;

import org.example.coursetrackingautomation.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCode(String code);

    List<Course> findByInstructorIdAndActiveTrue(Long instructorId);

    List<Course> findByActiveTrue();
}