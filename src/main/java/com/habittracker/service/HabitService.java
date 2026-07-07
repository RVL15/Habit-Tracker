package com.habittracker.service;

import com.habittracker.entity.Habit;
import com.habittracker.entity.User;
import com.habittracker.repository.HabitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HabitService {

    private final HabitRepository habitRepository;

    public List<Habit> getUserHabits(User user) {
        return habitRepository.findByUserOrderByDisplayOrderAscIdAsc(user);
    }

    public List<Habit> getActiveUserHabits(User user) {
        return habitRepository.findByUserAndActiveOrderByDisplayOrderAscIdAsc(user, true);
    }

    public Optional<Habit> getHabitById(Long id) {
        return habitRepository.findById(id);
    }

    public boolean existsByName(User user, String name) {
        return habitRepository.existsByUserAndHabitNameIgnoreCase(user, name.trim());
    }

    public boolean existsByNameAndNotId(User user, String name, Long id) {
        return habitRepository.existsByUserAndHabitNameIgnoreCaseAndIdNot(user, name.trim(), id);
    }

    @Transactional
    public Habit createHabit(Habit habit, User user) {
        String trimmedName = habit.getHabitName().trim();
        if (existsByName(user, trimmedName)) {
            throw new IllegalArgumentException("Habit name already exists");
        }
        habit.setUser(user);
        habit.setHabitName(trimmedName);
        habit.setActive(true);
        return habitRepository.save(habit);
    }

    @Transactional
    public Habit updateHabit(Long id, Habit updatedHabit, User user) {
        Habit existing = habitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Habit not found"));

        if (!existing.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Unauthorized access to habit");
        }

        String trimmedName = updatedHabit.getHabitName().trim();
        if (existsByNameAndNotId(user, trimmedName, id)) {
            throw new IllegalArgumentException("Habit name already exists");
        }

        existing.setHabitName(trimmedName);
        existing.setDescription(updatedHabit.getDescription());
        existing.setColor(updatedHabit.getColor());
        existing.setDisplayOrder(updatedHabit.getDisplayOrder());
        return habitRepository.save(existing);
    }

    @Transactional
    public void deleteHabit(Long id, User user) {
        Habit existing = habitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Habit not found"));

        if (!existing.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Unauthorized access to habit");
        }
        habitRepository.delete(existing);
    }

    @Transactional
    public void toggleActive(Long id, User user) {
        Habit existing = habitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Habit not found"));

        if (!existing.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Unauthorized access to habit");
        }
        existing.setActive(!existing.isActive());
        habitRepository.save(existing);
    }
}
