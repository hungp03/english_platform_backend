package com.english.api.admin.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {
    
    private UserStats users;
    private InstructorStats instructors;
    private CourseStats courses;
    private QuizStats quizzes;
    private RevenueStats revenue;
    private OrderStats orders;
    private PaymentStats payments;
    private LearningStats learning;
    private ContentStats content;
    
    @Data
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserStats {
        private Long total;
        private Long active;
        private Long verified;
        private Long inactive;
        private Long weekGrowth;
        private BigDecimal activePercentage;
        private BigDecimal verifiedPercentage;
    }
    
    @Data
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstructorStats {
        private Long totalInstructors;
        private Long pendingRequests;
        private Long pendingOver7Days;
        private Long pendingUnder3Days;
        private Long pending3To7Days;
    }
    
    @Data
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseStats {
        private Long totalCourses;
        private Long published;
        private Long draft;
        private Long archived;
        private Long totalModules;
        private Long totalLessons;
        private Long freeLessons;
        private BigDecimal publishedPercentage;
        private BigDecimal freeLessonsPercentage;
    }
    
    @Data
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuizStats {
        private Long totalQuizzes;
        private Long byReading;
        private Long byListening;
        private Long byWriting;
        private Long bySpeaking;
        private Long totalQuestions;
        private Long published;
        private Long draft;
    }
    
    @Data
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueStats {
        private Long totalCentsThisMonth;
        private String currency;
        private BigDecimal growthPercentage;
        private Long totalCentsVND;
    }
    
    @Data
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderStats {
        private Long totalOrdersThisMonth;
        private Long completed;
        private Long pending;
        private Long cancelled;
        private Long unpaidCarts;
        private BigDecimal completedPercentage;
        private BigDecimal averageOrderValue;
    }
    
    @Data
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentStats {
        private Long totalPayments;
        private Long byPayPal; 
        private Long byPayOS;
        private Long succeeded;
        private Long failed;
        private Long refunded;
        private BigDecimal successRate;
        private BigDecimal payPalPercentage;
        private BigDecimal payOSPercentage;
    }
    
    @Data
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LearningStats {
        private Long totalEnrollments;
        private Long completed;
        private Long suspended;
        private BigDecimal averageProgress;
        private Long totalAttempts;
    }
    
    @Data
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentStats {
        private Long totalBlogPosts;
        private Long publishedPosts;
        private Long draftPosts;
        private Long totalComments;
        private Long totalThreads;
        private Long totalForumPosts;
        private Long totalViews;
        private Long lockedThreads;
    }
}
