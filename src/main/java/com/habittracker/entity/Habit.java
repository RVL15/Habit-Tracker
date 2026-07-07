package com.habittracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "HABIT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Habit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "habit_name", nullable = false, length = 50)
    private String habitName;

    @Column(length = 255)
    private String description;

    @Column(length = 20)
    private String color;

    @Column(length = 50)
    private String category;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "habit", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<HabitTracker> trackers = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.color == null) {
            this.color = "#2ecc71"; // Default green accent color
        }
        if (this.category == null) {
            this.category = "Personal";
        }
        if (this.displayOrder == null) {
            this.displayOrder = 0;
        }
    }
}
