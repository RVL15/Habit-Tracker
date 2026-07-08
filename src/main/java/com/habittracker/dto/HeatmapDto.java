package com.habittracker.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeatmapDto {
    private int year;
    private List<HeatmapDayDto> days;
    private List<MonthLabel> monthLabels;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthLabel {
        private String name;
        private int colIndex;
    }
}
