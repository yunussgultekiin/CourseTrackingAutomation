package org.example.coursetrackingautomation.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "courses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
/**
 * Represents a course in the academic catalog.
 *
 * <p>A course is uniquely identified by its {@link #code}. Courses can be activated/deactivated,
 * have an assigned instructor, and support quota and weekly hour breakdowns used by the UI and
 * enrollment workflows.</p>
 */
public class Course extends BaseEntity {
    public static final int MAX_CODE_LENGTH = 50;
    public static final int MAX_NAME_LENGTH = 200;
    public static final int MAX_TERM_LENGTH = 100;

    @Column(nullable = false, unique = true, length = MAX_CODE_LENGTH)
    private String code;

    @Column(name = "name", nullable = false, length = MAX_NAME_LENGTH)
    private String name;

    @Column(name = "credit", nullable = false)
    private Integer credit;

    @Column(name = "quota", nullable = false)
    private Integer quota;

    @Column(name = "term", nullable = false, length = MAX_TERM_LENGTH)
    private String term;

    @Column(name = "weekly_total_hours")
    private Integer weeklyTotalHours;

    @Column(name = "weekly_theory_hours")
    private Integer weeklyTheoryHours;

    @Column(name = "weekly_practice_hours")
    private Integer weeklyPracticeHours;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    private User instructor;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private Set<Enrollment> enrollments = new HashSet<>();
}