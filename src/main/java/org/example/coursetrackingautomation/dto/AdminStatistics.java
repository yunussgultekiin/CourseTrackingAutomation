package org.example.coursetrackingautomation.dto;

/**
 * Aggregated statistics displayed on the admin dashboard.
 */
public record AdminStatistics(long totalUsers, long totalCourses, long activeEnrollments) {
}
