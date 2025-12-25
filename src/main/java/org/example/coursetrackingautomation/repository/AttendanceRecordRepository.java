package org.example.coursetrackingautomation.repository;

import org.example.coursetrackingautomation.entity.AttendanceRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

	Optional<AttendanceRecord> findFirstByEnrollmentIdOrderByWeekNumberDesc(Long enrollmentId);
}