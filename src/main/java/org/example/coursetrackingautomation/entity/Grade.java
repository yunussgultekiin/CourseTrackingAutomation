package org.example.coursetrackingautomation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Stores grading information for an {@link Enrollment}.
 *
 * <p>Scores are stored using {@link java.math.BigDecimal} to preserve precision. The average score,
 * letter grade, and pass/fail flag are derived values computed by the grading workflow.
 *
 * <p>There is a one-to-one relationship with {@link Enrollment} and the foreign key is unique,
 * ensuring that each enrollment has at most one grade record.
 */
@Entity
@Table(name = "grades")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Grade extends BaseEntity {
    public static final int SCORE_PRECISION = 10;
    public static final int SCORE_SCALE = 2;
    public static final int MAX_LETTER_GRADE_LENGTH = 20;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false, unique = true)
    private Enrollment enrollment;

    @Column(name = "midterm_score", precision = SCORE_PRECISION, scale = SCORE_SCALE)
    private BigDecimal midtermScore;

    @Column(name = "final_score", precision = SCORE_PRECISION, scale = SCORE_SCALE)
    private BigDecimal finalScore;

    @Column(name = "makeup_score", precision = SCORE_PRECISION, scale = SCORE_SCALE)
    private BigDecimal makeupScore;

    @Column(name = "average_score", precision = SCORE_PRECISION, scale = SCORE_SCALE)
    private BigDecimal averageScore;

    @Column(name = "letter_grade", length = MAX_LETTER_GRADE_LENGTH)
    private String letterGrade;

    @Column(name = "is_passed", nullable = false)
    private boolean passed;

}