package org.example.coursetrackingautomation.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.example.coursetrackingautomation.entity.Enrollment;
import org.example.coursetrackingautomation.entity.EnrollmentStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
/**
 * Spring Data repository for {@link Enrollment} persistence and queries.
 */
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
	/**
	 * Returns all enrollments for a student.
	 *
	 * @param studentId student identifier
	 * @return enrollments for the student
	 */
	List<Enrollment> findByStudentId(Long studentId);
	/**
	 * Returns all enrollments for a course.
	 *
	 * @param courseId course identifier
	 * @return enrollments for the course
	 */
	List<Enrollment> findByCourseId(Long courseId);

	@Query("select e from Enrollment e " +
			"join fetch e.student s " +
			"left join fetch e.grade g " +
			"where e.course.id = :courseId")
	/**
	 * Loads enrollments for a course and eagerly fetches student and grade associations.
	 *
	 * @param courseId course identifier
	 * @return enrollments with associated student and grade loaded
	 */
	List<Enrollment> findByCourseIdWithStudentAndGrade(@Param("courseId") Long courseId);

	@Query("select e from Enrollment e " +
			"join fetch e.student s " +
			"join fetch e.course c " +
			"where (:courseId is null or c.id = :courseId) " +
			"and (:status is null or e.status = :status) " +
			"and (:studentQuery is null or trim(:studentQuery) = '' " +
				"or lower(concat(coalesce(s.firstName, ''), ' ', coalesce(s.lastName, ''))) like concat('%', lower(:studentQuery), '%') " +
				"or lower(coalesce(s.username, '')) like concat('%', lower(:studentQuery), '%')) " +
			"order by e.enrollmentDate desc")
	/**
	 * Searches enrollments for admin views using optional filters.
	 *
	 * <p>All parameters are optional. When a parameter is {@code null} (or blank for studentQuery),
	 * the corresponding filter is not applied.</p>
	 *
	 * @param studentQuery partial match against student full name or username (case-insensitive)
	 * @param courseId optional course id filter
	 * @param status optional enrollment status filter
	 * @return matching enrollments with student and course eagerly fetched
	 */
	List<Enrollment> searchAdminEnrollments(
		@Param("studentQuery") String studentQuery,
		@Param("courseId") Long courseId,
		@Param("status") EnrollmentStatus status
	);

	/**
	 * Finds a student's enrollment in a given course.
	 *
	 * @param studentId student identifier
	 * @param courseId course identifier
	 * @return enrollment if present
	 */
	Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

	/**
	 * Counts enrollments by a set of statuses.
	 *
	 * @param statuses allowed statuses
	 * @return number of enrollments
	 */
	long countByStatusIn(Collection<EnrollmentStatus> statuses);
	/**
	 * Counts enrollments for a course by a set of statuses.
	 *
	 * @param courseId course identifier
	 * @param statuses allowed statuses
	 * @return number of matching enrollments
	 */
	long countByCourseIdAndStatusIn(Long courseId, Collection<EnrollmentStatus> statuses);
	/**
	 * Returns whether an enrollment exists for the student/course pair in any of the given statuses.
	 *
	 * @param studentId student identifier
	 * @param courseId course identifier
	 * @param statuses allowed statuses
	 * @return {@code true} if such an enrollment exists
	 */
	boolean existsByStudentIdAndCourseIdAndStatusIn(Long studentId, Long courseId, Collection<EnrollmentStatus> statuses);
	/**
	 * Returns the first enrollment matching the student/course pair and any of the given statuses.
	 *
	 * @param studentId student identifier
	 * @param courseId course identifier
	 * @param statuses allowed statuses
	 * @return the first matching enrollment
	 */
	Optional<Enrollment> findFirstByStudentIdAndCourseIdAndStatusIn(Long studentId, Long courseId, Collection<EnrollmentStatus> statuses);
	/**
	 * Returns enrollments for a course filtered by status.
	 *
	 * @param courseId course identifier
	 * @param statuses allowed statuses
	 * @return matching enrollments
	 */
	List<Enrollment> findByCourseIdAndStatusIn(Long courseId, Collection<EnrollmentStatus> statuses);
	/**
	 * Returns enrollments for a student filtered by status.
	 *
	 * @param studentId student identifier
	 * @param statuses allowed statuses
	 * @return matching enrollments
	 */
	List<Enrollment> findByStudentIdAndStatusIn(Long studentId, Collection<EnrollmentStatus> statuses);
}