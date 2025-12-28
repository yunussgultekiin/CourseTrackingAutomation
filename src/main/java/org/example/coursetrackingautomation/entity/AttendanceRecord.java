package org.example.coursetrackingautomation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents attendance for a single week/session within an {@link Enrollment}.
 *
 * <p>Attendance is tracked as a week number and a boolean presence flag, along with the calendar
 * date of the session. Aggregations (e.g., absenteeism counts and warning thresholds) are computed
 * in service-layer logic.
 */
@Entity
@Table(name = "attendance_records")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AttendanceRecord extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @Column(name = "week_number", nullable = false)
    private Integer weekNumber;

    @Column(name = "is_present", nullable = false)
    private boolean present;

    @Column(name = "date", nullable = false)
    private LocalDate date;

}