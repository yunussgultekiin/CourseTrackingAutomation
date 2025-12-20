package org.example.coursetrackingautomation.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
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
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class User extends BaseEntity {
    public static final int MAX_USERNAME_LENGTH = 150;
    public static final int MAX_PASSWORD_LENGTH = 255;
    public static final int MAX_NAME_LENGTH = 150;
    public static final int MAX_ROLE_LENGTH = 50;
    public static final int MAX_STUDENT_NUMBER_LENGTH = 100;
    public static final int MAX_EMAIL_LENGTH = 200;
    public static final int MAX_PHONE_LENGTH = 50;

    @Column(nullable = false, unique = true, length = MAX_USERNAME_LENGTH)
    private String username;

    @Column(nullable = false, length = MAX_PASSWORD_LENGTH)
    private String password;

    @Column(name = "first_name", nullable = false, length = MAX_NAME_LENGTH)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = MAX_NAME_LENGTH)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = MAX_ROLE_LENGTH)
    private Role role;

    @Column(name = "student_number", length = MAX_STUDENT_NUMBER_LENGTH)
    private String studentNumber;

    @Column(length = MAX_EMAIL_LENGTH)
    private String email;

    @Column(length = MAX_PHONE_LENGTH)
    private String phone;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "instructor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private Set<Course> instructedCourses = new HashSet<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private Set<Enrollment> enrollments = new HashSet<>();
}