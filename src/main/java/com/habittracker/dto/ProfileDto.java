package com.habittracker.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileDto {
    private String name;
    private String email;
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
    private String timezone;
    private int dailyGoal;
    private String theme;
    private boolean notificationsEnabled;
    private String profilePicture;
}
