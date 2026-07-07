package com.habittracker.service;

import com.habittracker.dto.InsightDto;
import com.habittracker.entity.Habit;
import com.habittracker.entity.HabitTracker;
import com.habittracker.entity.User;
import com.habittracker.repository.HabitRepository;
import com.habittracker.repository.HabitTrackerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InsightService {

    private final HabitRepository habitRepository;
    private final HabitTrackerRepository trackerRepository;

    public List<InsightDto> getInsights(User user) {
        List<Habit> activeHabits = habitRepository.findByUserAndActiveOrderByDisplayOrderAscIdAsc(user, true);
        if (activeHabits.isEmpty()) {
            return Collections.emptyList();
        }

        ZoneId zone = ZoneId.of(user.getTimezone() != null ? user.getTimezone() : "Asia/Kolkata");
        LocalDate today = LocalDate.now(zone);
        LocalDate startOfMonth = today.withDayOfMonth(1);

        List<HabitTracker> trackers = trackerRepository.findByHabitInAndTrackDateBetween(activeHabits, startOfMonth, today);
        List<InsightDto> insights = new ArrayList<>();

        addCompletionInsights(activeHabits, trackers, today.getDayOfMonth(), insights);
        addWeekdayInsights(trackers, insights);
        addMorningInsights(activeHabits, trackers, insights);

        if (insights.isEmpty()) {
            insights.add(new InsightDto("Info", "Start checking off your habits today to generate smart insights!"));
        }

        return insights;
    }

    private void addCompletionInsights(List<Habit> habits, List<HabitTracker> trackers, int days, List<InsightDto> insights) {
        Map<Long, Long> counts = trackers.stream()
                .filter(HabitTracker::isCompleted)
                .collect(Collectors.groupingBy(t -> t.getHabit().getId(), Collectors.counting()));

        for (Habit habit : habits) {
            long completed = counts.getOrDefault(habit.getId(), 0L);
            int rate = (int) ((completed * 100) / days);
            
            if (rate >= 80) {
                insights.add(new InsightDto("Success", "You complete \"" + habit.getHabitName() + "\" " + rate + "% of the time. Outstanding consistency!"));
            } else if (rate < 30) {
                insights.add(new InsightDto("Warning", "You missed \"" + habit.getHabitName() + "\" " + (days - completed) + " times this month. Try breaking it down into smaller steps."));
            }
        }
    }

    private void addWeekdayInsights(List<HabitTracker> trackers, List<InsightDto> insights) {
        Map<DayOfWeek, Long> weekdayCompletions = trackers.stream()
                .filter(HabitTracker::isCompleted)
                .collect(Collectors.groupingBy(t -> t.getTrackDate().getDayOfWeek(), Collectors.counting()));

        if (!weekdayCompletions.isEmpty()) {
            DayOfWeek worstDay = weekdayCompletions.entrySet().stream()
                    .min(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            if (worstDay != null) {
                String dayName = worstDay.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                insights.add(new InsightDto("Info", dayName + " has your lowest check-in frequency. Set a reminder to stay on track!"));
            }
        }
    }

    private void addMorningInsights(List<Habit> habits, List<HabitTracker> trackers, List<InsightDto> insights) {
        List<Habit> morningHabits = habits.stream()
                .filter(h -> h.getHabitName().toLowerCase().contains("morning") ||
                             h.getHabitName().toLowerCase().contains("am") ||
                             h.getHabitName().toLowerCase().contains("wake up"))
                .collect(Collectors.toList());

        if (!morningHabits.isEmpty()) {
            long totalCompleted = trackers.stream()
                    .filter(t -> morningHabits.contains(t.getHabit()) && t.isCompleted())
                    .count();
            
            if (totalCompleted > 0) {
                insights.add(new InsightDto("Success", "Your morning habits show positive momentum. Keep starting your day strong!"));
            }
        }
    }
}
