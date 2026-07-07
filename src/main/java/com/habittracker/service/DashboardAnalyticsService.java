package com.habittracker.service;

import com.habittracker.dto.DashboardAnalyticsDto;
import com.habittracker.dto.DashboardAnalyticsDto.DayStats;
import com.habittracker.dto.DashboardAnalyticsDto.MonthStats;
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
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardAnalyticsService {

    private final HabitRepository habitRepository;
    private final HabitTrackerRepository trackerRepository;

    private static final String[] QUOTES = {
        "Success is the sum of small efforts, repeated day in and day out.",
        "Quality is not an act, it is a habit.",
        "Motivation is what gets you started. Habit is what keeps you going.",
        "Small daily improvements over time lead to stunning results.",
        "It is easier to prevent bad habits than to break them.",
        "Your habits will determine your future."
    };

    public DashboardAnalyticsDto getDashboardAnalytics(User user) {
        List<Habit> activeHabits = habitRepository.findByUserAndActiveOrderByDisplayOrderAscIdAsc(user, true);
        if (activeHabits.isEmpty()) {
            return getEmptyAnalytics();
        }

        ZoneId zone = ZoneId.of("Asia/Kolkata");
        LocalDate today = LocalDate.now(zone);
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate queryStartDate = startOfWeek.isBefore(startOfMonth) ? startOfWeek : startOfMonth;

        List<HabitTracker> trackers = trackerRepository.findByHabitInAndTrackDateBetween(activeHabits, queryStartDate, today);
        Map<LocalDate, List<HabitTracker>> trackersByDate = trackers.stream()
                .filter(HabitTracker::isCompleted)
                .collect(Collectors.groupingBy(HabitTracker::getTrackDate));

        int completedToday = trackersByDate.getOrDefault(today, Collections.emptyList()).size();
        int pendingToday = activeHabits.size() - completedToday;
        int todayCompletion = (completedToday * 100) / activeHabits.size();

        List<DayStats> weeklyStats = calculateWeeklyStatistics(startOfWeek, today, activeHabits.size(), trackersByDate);
        MonthStats monthlyStats = calculateMonthlyStatistics(startOfMonth, today, activeHabits.size(), trackersByDate);
        
        int currentStreak = calculateCurrentStreak(today, activeHabits.size(), trackersByDate);
        int longestStreak = calculateLongestStreak(queryStartDate, today, activeHabits.size(), trackersByDate);
        
        String bestHabit = calculateBestHabit(activeHabits, trackers);
        String worstHabit = calculateWorstHabit(activeHabits, trackers);

        return DashboardAnalyticsDto.builder()
                .totalHabits(activeHabits.size())
                .completedToday(completedToday)
                .pendingToday(pendingToday)
                .todayCompletion(todayCompletion)
                .weeklyCompletion(calculateWeeklyCompletionPercent(weeklyStats))
                .monthlyCompletion(monthlyStats.getAverageCompletion())
                .overallCompletion(monthlyStats.getAverageCompletion()) // Overall default to monthly for current workspace limit
                .currentStreak(currentStreak)
                .longestStreak(longestStreak)
                .bestHabit(bestHabit)
                .worstHabit(worstHabit)
                .motivationalQuote(getRandomQuote())
                .weeklyStatistics(weeklyStats)
                .monthlyStatistics(monthlyStats)
                .build();
    }

    private List<DayStats> calculateWeeklyStatistics(LocalDate startOfWeek, LocalDate today, int totalHabits, Map<LocalDate, List<HabitTracker>> trackersByDate) {
        List<DayStats> list = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = startOfWeek.plusDays(i);
            int completed = trackersByDate.getOrDefault(date, Collections.emptyList()).size();
            int pending = totalHabits - completed;
            int percent = (completed * 100) / totalHabits;
            
            String dayName = date.getDayOfWeek().name().substring(0, 1) + date.getDayOfWeek().name().substring(1).toLowerCase();
            list.add(new DayStats(dayName, percent, completed, pending, date.equals(today)));
        }
        return list;
    }

    private MonthStats calculateMonthlyStatistics(LocalDate startOfMonth, LocalDate today, int totalHabits, Map<LocalDate, List<HabitTracker>> trackersByDate) {
        int completedDays = 0;
        int missedDays = 0;
        int perfectDays = 0;
        int totalCompletionSum = 0;
        int daysCount = today.getDayOfMonth();

        for (int d = 1; d <= daysCount; d++) {
            LocalDate date = startOfMonth.withDayOfMonth(d);
            int completed = trackersByDate.getOrDefault(date, Collections.emptyList()).size();
            
            if (completed > 0) {
                completedDays++;
            } else {
                missedDays++;
            }
            if (completed == totalHabits) {
                perfectDays++;
            }
            totalCompletionSum += (completed * 100) / totalHabits;
        }

        return MonthStats.builder()
                .completedDaysCount(completedDays)
                .missedDaysCount(missedDays)
                .perfectDaysCount(perfectDays)
                .averageCompletion(totalCompletionSum / daysCount)
                .build();
    }

    private int calculateCurrentStreak(LocalDate today, int totalHabits, Map<LocalDate, List<HabitTracker>> trackersByDate) {
        int streak = 0;
        LocalDate date = today;
        
        // If today is not perfect, check if yesterday was perfect, to allow today to be tracked still
        if (trackersByDate.getOrDefault(date, Collections.emptyList()).size() < totalHabits) {
            date = date.minusDays(1);
        }

        while (true) {
            int completed = trackersByDate.getOrDefault(date, Collections.emptyList()).size();
            if (completed == totalHabits && totalHabits > 0) {
                streak++;
                date = date.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }

    private int calculateLongestStreak(LocalDate queryStartDate, LocalDate today, int totalHabits, Map<LocalDate, List<HabitTracker>> trackersByDate) {
        int maxStreak = 0;
        int currentStreak = 0;
        
        for (LocalDate date = queryStartDate; !date.isAfter(today); date = date.plusDays(1)) {
            int completed = trackersByDate.getOrDefault(date, Collections.emptyList()).size();
            if (completed == totalHabits && totalHabits > 0) {
                currentStreak++;
                if (currentStreak > maxStreak) {
                    maxStreak = currentStreak;
                }
            } else {
                currentStreak = 0;
            }
        }
        return maxStreak;
    }

    private String calculateBestHabit(List<Habit> habits, List<HabitTracker> trackers) {
        Map<Long, Long> counts = trackers.stream()
                .filter(HabitTracker::isCompleted)
                .collect(Collectors.groupingBy(t -> t.getHabit().getId(), Collectors.counting()));

        return habits.stream()
                .max(Comparator.comparingLong(h -> counts.getOrDefault(h.getId(), 0L)))
                .map(Habit::getHabitName)
                .orElse("None");
    }

    private String calculateWorstHabit(List<Habit> habits, List<HabitTracker> trackers) {
        Map<Long, Long> counts = trackers.stream()
                .filter(HabitTracker::isCompleted)
                .collect(Collectors.groupingBy(t -> t.getHabit().getId(), Collectors.counting()));

        return habits.stream()
                .min(Comparator.comparingLong(h -> counts.getOrDefault(h.getId(), 0L)))
                .map(Habit::getHabitName)
                .orElse("None");
    }

    private int calculateWeeklyCompletionPercent(List<DayStats> stats) {
        return (int) stats.stream().mapToInt(DayStats::getCompletionPercent).average().orElse(0);
    }

    private String getRandomQuote() {
        return QUOTES[new Random().nextInt(QUOTES.length)];
    }

    private DashboardAnalyticsDto getEmptyAnalytics() {
        return DashboardAnalyticsDto.builder()
                .totalHabits(0)
                .bestHabit("None")
                .worstHabit("None")
                .motivationalQuote(getRandomQuote())
                .weeklyStatistics(Collections.emptyList())
                .monthlyStatistics(new MonthStats(0, 0, 0, 0))
                .build();
    }
}
