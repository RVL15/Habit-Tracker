package com.habittracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
    name = "HABIT_TRACKER",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"habit_id", "track_date"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HabitTracker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habit_id", nullable = false)
    private Habit habit;

    @Column(name = "track_date", nullable = false)
    private LocalDate trackDate;

    @Column(nullable = false)
    private boolean completed;
}
