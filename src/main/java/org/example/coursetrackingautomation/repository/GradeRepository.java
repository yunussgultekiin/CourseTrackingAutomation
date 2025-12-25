package org.example.coursetrackingautomation.repository;

import java.util.Optional;
import org.example.coursetrackingautomation.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
	Optional<Grade> findByEnrollmentId(Long enrollmentId);
}