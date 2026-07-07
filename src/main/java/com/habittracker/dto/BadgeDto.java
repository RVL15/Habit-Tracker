package com.habittracker.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BadgeDto {
    private String name;
    private String description;
    private String icon;
    private boolean unlocked;
    private String unlockedAt;
}
