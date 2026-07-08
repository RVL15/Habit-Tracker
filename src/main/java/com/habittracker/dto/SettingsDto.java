package com.habittracker.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettingsDto {
    private String theme;
    private String language;
    private String timezone;
    private String startDay;
    private String weekFormat;
    private String notificationTime;
    private boolean notificationsEnabled;
}
