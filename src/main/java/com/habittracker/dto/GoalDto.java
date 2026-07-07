package com.habittracker.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalDto {
    private String goalType; // "Daily", "Weekly", "Monthly"
    private String targetDescription;
    private int targetValue;
    private int actualValue;
    private String statusColor; // "success" (Green), "warning" (Yellow), "danger" (Red)
}
