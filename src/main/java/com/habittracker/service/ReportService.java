package com.habittracker.service;

import com.habittracker.dto.ReportDto;
import com.habittracker.entity.Habit;
import com.habittracker.entity.HabitTracker;
import com.habittracker.entity.User;
import com.habittracker.repository.HabitRepository;
import com.habittracker.repository.HabitTrackerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final HabitRepository habitRepository;
    private final HabitTrackerRepository trackerRepository;
    private final DashboardAnalyticsService analyticsService;

    public ReportDto getMonthlyReport(User user) {
        List<Habit> activeHabits = habitRepository.findByUserAndActiveOrderByDisplayOrderAscIdAsc(user, true);
        if (activeHabits.isEmpty()) {
            return getEmptyReport();
        }

        ZoneId zone = ZoneId.of(user.getTimezone() != null ? user.getTimezone() : "Asia/Kolkata");
        LocalDate today = LocalDate.now(zone);
        LocalDate startOfMonth = today.withDayOfMonth(1);

        List<HabitTracker> currentTrackers = trackerRepository.findByHabitInAndTrackDateBetween(activeHabits, startOfMonth, today);
        Map<LocalDate, List<HabitTracker>> grouped = currentTrackers.stream()
                .filter(HabitTracker::isCompleted)
                .collect(Collectors.groupingBy(HabitTracker::getTrackDate));

        int completedDays = 0;
        int perfectDays = 0;
        int totalCompletionSum = 0;
        int daysCount = today.getDayOfMonth();

        for (int d = 1; d <= daysCount; d++) {
            LocalDate date = startOfMonth.withDayOfMonth(d);
            int completed = grouped.getOrDefault(date, Collections.emptyList()).size();
            if (completed > 0) completedDays++;
            if (completed == activeHabits.size()) perfectDays++;
            totalCompletionSum += (completed * 100) / activeHabits.size();
        }

        int currentAvg = totalCompletionSum / daysCount;
        int improvement = calculateImprovement(user, today, activeHabits, currentAvg);

        var analytics = analyticsService.getDashboardAnalytics(user);

        return ReportDto.builder()
                .completedDays(completedDays)
                .missedDays(daysCount - completedDays)
                .perfectDays(perfectDays)
                .currentStreak(analytics.getCurrentStreak())
                .longestStreak(analytics.getLongestStreak())
                .averagePercent(currentAvg)
                .bestHabit(analytics.getBestHabit())
                .worstHabit(analytics.getWorstHabit())
                .improvementPercent(improvement)
                .build();
    }

    private int calculateImprovement(User user, LocalDate today, List<Habit> activeHabits, int currentAvg) {
        LocalDate startOfPrevMonth = today.minusMonths(1).withDayOfMonth(1);
        LocalDate endOfPrevMonth = startOfPrevMonth.withDayOfMonth(startOfPrevMonth.lengthOfMonth());
        
        List<HabitTracker> prevTrackers = trackerRepository.findByHabitInAndTrackDateBetween(activeHabits, startOfPrevMonth, endOfPrevMonth);
        long completedPrev = prevTrackers.stream().filter(HabitTracker::isCompleted).count();
        int totalPrevPossible = activeHabits.size() * startOfPrevMonth.lengthOfMonth();
        int prevAvg = totalPrevPossible > 0 ? (int) ((completedPrev * 100) / totalPrevPossible) : 0;

        return currentAvg - prevAvg;
    }

    private ReportDto getEmptyReport() {
        return ReportDto.builder()
                .bestHabit("None")
                .worstHabit("None")
                .build();
    }
}
