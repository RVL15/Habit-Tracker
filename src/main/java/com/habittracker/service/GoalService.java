package com.habittracker.service;

import com.habittracker.dto.GoalDto;
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
public class GoalService {

    private final HabitRepository habitRepository;
    private final HabitTrackerRepository trackerRepository;

    public List<GoalDto> getGoals(User user) {
        List<Habit> activeHabits = habitRepository.findByUserAndActiveOrderByDisplayOrderAscIdAsc(user, true);
        if (activeHabits.isEmpty()) {
            return Collections.emptyList();
        }

        ZoneId zone = ZoneId.of(user.getTimezone() != null ? user.getTimezone() : "Asia/Kolkata");
        LocalDate today = LocalDate.now(zone);

        List<GoalDto> goals = new ArrayList<>();
        goals.add(calculateDailyGoal(user, today, activeHabits));
        goals.add(calculateWeeklyGoal(user, today, activeHabits));
        goals.add(calculateMonthlyGoal(user, today, activeHabits));

        return goals;
    }

    private GoalDto calculateDailyGoal(User user, LocalDate today, List<Habit> activeHabits) {
        List<HabitTracker> todayTrackers = trackerRepository.findByHabitInAndTrackDateBetween(activeHabits, today, today);
        long completedToday = todayTrackers.stream().filter(HabitTracker::isCompleted).count();
        int target = user.getDailyGoal() != null ? user.getDailyGoal() : 3;

        String color = "danger";
        if (completedToday >= target) {
            color = "success";
        } else if (completedToday > 0) {
            color = "warning";
        }

        return GoalDto.builder()
                .goalType("Daily")
                .targetDescription("Complete " + target + " habits today")
                .targetValue(target)
                .actualValue((int) completedToday)
                .statusColor(color)
                .build();
    }

    private GoalDto calculateWeeklyGoal(User user, LocalDate today, List<Habit> activeHabits) {
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        List<HabitTracker> weeklyTrackers = trackerRepository.findByHabitInAndTrackDateBetween(activeHabits, startOfWeek, today);
        long completed = weeklyTrackers.stream().filter(HabitTracker::isCompleted).count();
        int totalPossible = activeHabits.size() * (today.getDayOfWeek().getValue());
        int actualPercent = totalPossible > 0 ? (int) ((completed * 100) / totalPossible) : 0;
        int targetPercent = 90;

        String color = "danger";
        if (actualPercent >= targetPercent) {
            color = "success";
        } else if (actualPercent >= 50) {
            color = "warning";
        }

        return GoalDto.builder()
                .goalType("Weekly")
                .targetDescription("Complete " + targetPercent + "% of weekly check-ins")
                .targetValue(targetPercent)
                .actualValue(actualPercent)
                .statusColor(color)
                .build();
    }

    private GoalDto calculateMonthlyGoal(User user, LocalDate today, List<Habit> activeHabits) {
        LocalDate startOfMonth = today.withDayOfMonth(1);
        List<HabitTracker> monthlyTrackers = trackerRepository.findByHabitInAndTrackDateBetween(activeHabits, startOfMonth, today);
        Map<LocalDate, List<HabitTracker>> grouped = monthlyTrackers.stream()
                .filter(HabitTracker::isCompleted)
                .collect(Collectors.groupingBy(HabitTracker::getTrackDate));

        int perfectDays = 0;
        for (int d = 1; d <= today.getDayOfMonth(); d++) {
            LocalDate date = startOfMonth.withDayOfMonth(d);
            if (grouped.getOrDefault(date, Collections.emptyList()).size() == activeHabits.size()) {
                perfectDays++;
            }
        }
        int targetPerfectDays = 20;

        String color = "danger";
        if (perfectDays >= targetPerfectDays) {
            color = "success";
        } else if (perfectDays >= 10) {
            color = "warning";
        }

        return GoalDto.builder()
                .goalType("Monthly")
                .targetDescription("Achieve " + targetPerfectDays + " perfect days this month")
                .targetValue(targetPerfectDays)
                .actualValue(perfectDays)
                .statusColor(color)
                .build();
    }
}
