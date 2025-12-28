package org.example.coursetrackingautomation.repository;

import java.util.Optional;
import org.example.coursetrackingautomation.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
/**
 * Spring Data repository for {@link Grade} persistence.
 */
public interface GradeRepository extends JpaRepository<Grade, Long> {
	/**
	 * Finds a grade record by its enrollment id.
	 *
	 * @param enrollmentId enrollment identifier
	 * @return grade if present
	 */
	Optional<Grade> findByEnrollmentId(Long enrollmentId);
}