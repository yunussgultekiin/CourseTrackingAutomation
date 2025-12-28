package org.example.coursetrackingautomation.repository;

import org.example.coursetrackingautomation.entity.AttendanceRecord;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
/**
 * Spring Data repository for {@link AttendanceRecord} persistence and attendance queries.
 */
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

	/**
	 * Returns the most recent attendance record for a given enrollment.
	 *
	 * @param enrollmentId enrollment identifier
	 * @return the latest attendance record if present
	 */
	Optional<AttendanceRecord> findFirstByEnrollmentIdOrderByWeekNumberDesc(Long enrollmentId);

	/**
	 * Finds attendance record by enrollment id and week number.
	 *
	 * @param enrollmentId enrollment identifier
	 * @param weekNumber week number
	 * @return record if present
	 */
	Optional<AttendanceRecord> findByEnrollmentIdAndWeekNumber(Long enrollmentId, Integer weekNumber);

	@Query("select ar from AttendanceRecord ar join fetch ar.enrollment e where e.id in :enrollmentIds and ar.weekNumber = :weekNumber")
	/**
	 * Loads attendance records for a set of enrollments in a specific week, fetching the enrollment association.
	 *
	 * @param enrollmentIds enrollment identifiers
	 * @param weekNumber week number
	 * @return matching attendance records
	 */
	List<AttendanceRecord> findByEnrollmentIdsAndWeekNumberWithEnrollment(
		@Param("enrollmentIds") Collection<Long> enrollmentIds,
		@Param("weekNumber") Integer weekNumber
	);

	@Query("select max(ar.weekNumber) from AttendanceRecord ar where ar.enrollment.course.id = :courseId")
	/**
	 * Returns the maximum recorded week number for a course.
	 *
	 * @param courseId course identifier
	 * @return maximum week number, or {@code null} if no attendance is recorded
	 */
	Integer findMaxWeekNumberByCourseId(@Param("courseId") Long courseId);
}