package com.habittracker.service;

import com.habittracker.entity.Habit;
import com.habittracker.entity.HabitTracker;
import com.habittracker.entity.User;
import com.habittracker.repository.HabitRepository;
import com.habittracker.repository.HabitTrackerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class HabitTrackerService {

    private final HabitTrackerRepository trackerRepository;
    private final HabitRepository habitRepository;

    @Transactional
    public void trackHabit(Long habitId, LocalDate date, boolean completed, User user) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new IllegalArgumentException("Habit not found"));

        if (!habit.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Unauthorized access to habit");
        }

        if (!habit.isActive()) {
            throw new IllegalArgumentException("Cannot track inactive habit");
        }

        Optional<HabitTracker> trackerOpt = trackerRepository.findByHabitAndTrackDate(habit, date);

        if (trackerOpt.isPresent()) {
            HabitTracker tracker = trackerOpt.get();
            tracker.setCompleted(completed);
            trackerRepository.save(tracker);
        } else {
            HabitTracker tracker = HabitTracker.builder()
                    .habit(habit)
                    .trackDate(date)
                    .completed(completed)
                    .build();
            trackerRepository.save(tracker);
        }
    }

    public Map<Long, Set<LocalDate>> getMonthlyTrackerMap(List<Habit> habits, int year, int month) {
        Map<Long, Set<LocalDate>> map = new HashMap<>();
        if (habits.isEmpty()) {
            return map;
        }

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<HabitTracker> trackers = trackerRepository.findByHabitInAndTrackDateBetween(habits, startDate, endDate);

        for (HabitTracker tracker : trackers) {
            if (tracker.isCompleted()) {
                Long habitId = tracker.getHabit().getId();
                LocalDate date = tracker.getTrackDate();
                map.computeIfAbsent(habitId, k -> new HashSet<>()).add(date);
            }
        }
        return map;
    }
}
