package com.habittracker.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardAnalyticsDto {

    private int todayCompletion;
    private int weeklyCompletion;
    private int monthlyCompletion;
    private int overallCompletion;
    
    private int currentStreak;
    private int longestStreak;
    
    private int completedToday;
    private int pendingToday;
    
    private String bestHabit;
    private String worstHabit;
    private int totalHabits;
    
    private String motivationalQuote;
    
    private List<DayStats> weeklyStatistics;
    private MonthStats monthlyStatistics;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DayStats {
        private String dayName;        // e.g. "Monday"
        private int completionPercent; // e.g. 80
        private int completedCount;    // e.g. 4
        private int pendingCount;      // e.g. 1
        private boolean isToday;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class MonthStats {
        private int completedDaysCount;
        private int missedDaysCount;
        private int perfectDaysCount;
        private int averageCompletion;
    }
}
