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

    @Column(length = 50)
    private String language;

    @Column(name = "notification_time", length = 10)
    private String notificationTime;

    @Column(name = "start_day", length = 20)
    private String startDay;

    @Column(name = "week_format", length = 20)
    private String weekFormat;

    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<Habit> habits = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<Badge> badges = new java.util.ArrayList<>();

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
        if (this.language == null) {
            this.language = "en";
        }
        if (this.notificationTime == null) {
            this.notificationTime = "08:00";
        }
        if (this.startDay == null) {
            this.startDay = "Monday";
        }
        if (this.weekFormat == null) {
            this.weekFormat = "Mon-Sun";
        }
        if (this.role == null) {
            this.role = "ROLE_USER";
        }
    }
}
