package org.example.coursetrackingautomation.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.example.coursetrackingautomation.entity.Enrollment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
	List<Enrollment> findByStudentId(Long studentId);
	List<Enrollment> findByCourseId(Long courseId);

	@Query("select e from Enrollment e " +
			"join fetch e.student s " +
			"left join fetch e.grade g " +
			"where e.course.id = :courseId")
	List<Enrollment> findByCourseIdWithStudentAndGrade(@Param("courseId") Long courseId);

	Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

	long countByStatusIn(Collection<String> statuses);
	long countByCourseIdAndStatusIn(Long courseId, Collection<String> statuses);
	boolean existsByStudentIdAndCourseIdAndStatusIn(Long studentId, Long courseId, Collection<String> statuses);
	Optional<Enrollment> findFirstByStudentIdAndCourseIdAndStatusIn(Long studentId, Long courseId, Collection<String> statuses);
	List<Enrollment> findByCourseIdAndStatusIn(Long courseId, Collection<String> statuses);
	List<Enrollment> findByStudentIdAndStatusIn(Long studentId, Collection<String> statuses);
}