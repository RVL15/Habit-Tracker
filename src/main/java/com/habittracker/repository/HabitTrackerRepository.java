package com.habittracker.repository;

import com.habittracker.entity.Habit;
import com.habittracker.entity.HabitTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HabitTrackerRepository extends JpaRepository<HabitTracker, Long> {
    Optional<HabitTracker> findByHabitAndTrackDate(Habit habit, LocalDate trackDate);
    List<HabitTracker> findByHabitInAndTrackDateBetween(List<Habit> habits, LocalDate startDate, LocalDate endDate);
}
