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
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

	Optional<AttendanceRecord> findFirstByEnrollmentIdOrderByWeekNumberDesc(Long enrollmentId);

	Optional<AttendanceRecord> findByEnrollmentIdAndWeekNumber(Long enrollmentId, Integer weekNumber);

	@Query("select ar from AttendanceRecord ar join fetch ar.enrollment e where e.id in :enrollmentIds and ar.weekNumber = :weekNumber")
	List<AttendanceRecord> findByEnrollmentIdsAndWeekNumberWithEnrollment(
		@Param("enrollmentIds") Collection<Long> enrollmentIds,
		@Param("weekNumber") Integer weekNumber
	);

	@Query("select max(ar.weekNumber) from AttendanceRecord ar where ar.enrollment.course.id = :courseId")
	Integer findMaxWeekNumberByCourseId(@Param("courseId") Long courseId);
}