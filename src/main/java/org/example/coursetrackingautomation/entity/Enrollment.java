package org.example.coursetrackingautomation.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a student's enrollment in a specific {@link Course}.
 *
 * <p>This entity links a {@link User student} to a course and stores enrollment-specific state such
 * as attendance-derived absenteeism counts, administrative status (e.g., active/dropped), and the
 * time the enrollment was created.
 *
 * <p>Associated data includes an optional {@link Grade} (one-to-one) and a set of
 * {@link AttendanceRecord attendance records} (one-to-many).
 */
@Entity
@Table(name = "enrollments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Enrollment extends BaseEntity {
    public static final int STATUS_MAX_LENGTH = 50;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "absenteeism_count", nullable = false)
    private Integer absenteeismCount;

    @Column(name = "status", nullable = false, length = STATUS_MAX_LENGTH)
    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status;

    @Column(name = "enrollment_date", nullable = false)
    private LocalDateTime enrollmentDate;

    @OneToOne(mappedBy = "enrollment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Grade grade;

    @OneToMany(mappedBy = "enrollment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private Set<AttendanceRecord> attendanceRecords = new HashSet<>();
}