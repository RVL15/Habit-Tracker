package com.habittracker.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeatmapDayDto {
    private String date;
    private String displayDate;
    private int completedCount;
    private int totalCount;
    private int completionPercent;
    private String intensityClass;
    private String tooltip;
    private boolean isToday;
    private int row; // grid-row (1 to 7)
    private int col; // grid-column (1 to 54)
    private String completedHabits; // comma separated list for UI modal
    private String missedHabits; // comma separated list for UI modal
}
