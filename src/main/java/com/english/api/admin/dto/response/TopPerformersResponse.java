package com.english.api.admin.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopPerformersResponse {
    
    private List<TopCourse> topCourses;
    private List<TopInstructor> topInstructors;
    private List<TopRevenueCourse> topRevenueCourses;
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopCourse {
        private UUID id;
        private String title;
        private String slug;
        private String thumbnail;
        private String instructorName;
        private UUID instructorId;
        private Long enrollmentCount;
        private Long completionCount;
        private BigDecimal completionRate;
        private BigDecimal averageRating;
        private Long totalRevenueCents;
        private String currency;
        private Integer rank;
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopInstructor {
        private UUID id;
        private String fullName;
        private String email;
        private String avatarUrl;
        private Long totalCourses;
        private Long publishedCourses;
        private Long totalEnrollments;
        private Long totalStudents;
        private BigDecimal averageRating;
        private Long totalRevenueCents;
        private Integer rank;
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopRevenueCourse {
        private UUID id;
        private String title;
        private String slug;
        private String instructorName;
        private Long totalRevenueCents;
        private String currency;
        private Long totalOrders;
        private Long enrollmentCount;
        private BigDecimal averageOrderValue;
        private Integer rank;
    }
}
