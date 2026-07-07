package com.habittracker.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsightDto {
    private String type; // "Success" (green), "Warning" (yellow), "Info" (blue)
    private String text;
}
