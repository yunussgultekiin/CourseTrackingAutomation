package org.example.coursetrackingautomation.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "attendance_records")
@Data
public class AttendanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}