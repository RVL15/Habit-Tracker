package com.habittracker.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDto {
    private int completedDays;
    private int missedDays;
    private int perfectDays;
    private int currentStreak;
    private int longestStreak;
    private int averagePercent;
    private String bestHabit;
    private String worstHabit;
    private int improvementPercent;
}
