package com.habittracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "USER")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 100)
    private String timezone;

    @Column(name = "daily_goal")
    private Integer dailyGoal;

    @Column(length = 20)
    private String theme;

    @Column(name = "notifications_enabled")
    private Boolean notificationsEnabled;

    @Column(name = "profile_picture", length = 100)
    private String profilePicture;

    public boolean isNotificationsEnabled() {
        return notificationsEnabled != null && notificationsEnabled;
    }

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.timezone == null) {
            this.timezone = "Asia/Kolkata";
        }
        if (this.dailyGoal == null) {
            this.dailyGoal = 3;
        }
        if (this.theme == null) {
            this.theme = "light";
        }
        if (this.notificationsEnabled == null) {
            this.notificationsEnabled = true;
        }
        if (this.profilePicture == null) {
            this.profilePicture = "👤";
        }
    }
}
