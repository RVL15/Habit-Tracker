package com.habittracker.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoryDto {
    private String dateStr; // e.g. "07 Jul"
    private List<HabitStatus> habitStatuses;
    
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class HabitStatus {
        private String habitName;
        private boolean completed;
        private String color;
    }
}
