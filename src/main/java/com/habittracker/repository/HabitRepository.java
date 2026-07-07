package com.habittracker.repository;

import com.habittracker.entity.Habit;
import com.habittracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HabitRepository extends JpaRepository<Habit, Long> {
    List<Habit> findByUserOrderByDisplayOrderAscIdAsc(User user);
    List<Habit> findByUserAndActiveOrderByDisplayOrderAscIdAsc(User user, boolean active);
    boolean existsByUserAndHabitNameIgnoreCase(User user, String habitName);
    boolean existsByUserAndHabitNameIgnoreCaseAndIdNot(User user, String habitName, Long id);
}
