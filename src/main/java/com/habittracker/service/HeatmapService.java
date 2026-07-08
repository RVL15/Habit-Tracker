package com.habittracker.service;

import com.habittracker.dto.HeatmapDayDto;
import com.habittracker.dto.HeatmapDto;
import com.habittracker.entity.Habit;
import com.habittracker.entity.HabitTracker;
import com.habittracker.entity.User;
import com.habittracker.repository.HabitRepository;
import com.habittracker.repository.HabitTrackerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HeatmapService {

    private final HabitRepository habitRepository;
    private final HabitTrackerRepository trackerRepository;

    public HeatmapDto getHeatmap(User user, int year) {
        List<Habit> activeHabits = habitRepository.findByUserAndActiveOrderByDisplayOrderAscIdAsc(user, true);
        LocalDate startOfYear = LocalDate.of(year, 1, 1);
        LocalDate endOfYear = LocalDate.of(year, 12, 31);

        if (activeHabits.isEmpty()) {
            return HeatmapDto.builder()
                    .year(year)
                    .days(Collections.emptyList())
                    .monthLabels(Collections.emptyList())
                    .build();
        }

        // Single batch query for the entire year
        List<HabitTracker> trackers = trackerRepository.findByHabitInAndTrackDateBetween(activeHabits, startOfYear, endOfYear);
        Map<LocalDate, List<HabitTracker>> trackersByDate = trackers.stream()
                .collect(Collectors.groupingBy(HabitTracker::getTrackDate));

        LocalDate jan1 = LocalDate.of(year, 1, 1);
        int jan1DayOfWeek = jan1.getDayOfWeek().getValue() % 7; // Sunday=0, Monday=1, ..., Saturday=6

        List<HeatmapDayDto> days = new ArrayList<>();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        DateTimeFormatter displayDtf = DateTimeFormatter.ofPattern("dd MMM yyyy");

        for (LocalDate date = startOfYear; !date.isAfter(endOfYear); date = date.plusDays(1)) {
            LocalDate finalDate = date;
            List<Habit> habitsOnDate = activeHabits.stream()
                    .filter(h -> h.getCreatedAt().toLocalDate().isBefore(finalDate) || h.getCreatedAt().toLocalDate().isEqual(finalDate))
                    .collect(Collectors.toList());

            int totalCount = habitsOnDate.size();
            List<HabitTracker> trackersOnDate = trackersByDate.getOrDefault(date, Collections.emptyList());
            
            List<String> completedHabits = new ArrayList<>();
            List<String> missedHabits = new ArrayList<>();

            for (Habit h : habitsOnDate) {
                boolean completed = trackersOnDate.stream()
                        .anyMatch(t -> t.getHabit().getId().equals(h.getId()) && t.isCompleted());
                if (completed) {
                    completedHabits.add(h.getHabitName());
                } else {
                    missedHabits.add(h.getHabitName());
                }
            }

            int completedCount = completedHabits.size();
            int percent = totalCount > 0 ? (completedCount * 100) / totalCount : 0;

            String intensityClass = "heatmap-level-0";
            if (totalCount > 0 && completedCount > 0) {
                if (completedCount == totalCount) {
                    intensityClass = "heatmap-level-perfect";
                } else if (percent >= 80) {
                    intensityClass = "heatmap-level-5";
                } else if (percent >= 60) {
                    intensityClass = "heatmap-level-4";
                } else if (percent >= 40) {
                    intensityClass = "heatmap-level-3";
                } else if (percent >= 20) {
                    intensityClass = "heatmap-level-2";
                } else {
                    intensityClass = "heatmap-level-1";
                }
            }

            // Placements for CSS Grid: row (Sunday=1, Monday=2, ..., Saturday=7)
            int dayOfWeekVal = date.getDayOfWeek().getValue() % 7 + 1;
            int colIndex = (date.getDayOfYear() + jan1DayOfWeek - 1) / 7 + 1;

            boolean isToday = date.equals(today);
            String tooltip = "<strong>" + date.format(displayDtf) + "</strong><br/>" +
                    "Completed: " + completedCount + "/" + totalCount + " (" + percent + "%)<br/>" +
                    "Status: " + (percent == 100 ? "Perfect Day" : (percent == 0 ? "No Activity" : "Active")) + "<br/>" +
                    "Week: " + colIndex + ", Day: " + date.getDayOfWeek().name().substring(0, 1) + date.getDayOfWeek().name().substring(1).toLowerCase();

            days.add(HeatmapDayDto.builder()
                    .date(date.toString())
                    .displayDate(date.format(displayDtf))
                    .completedCount(completedCount)
                    .totalCount(totalCount)
                    .completionPercent(percent)
                    .intensityClass(intensityClass)
                    .tooltip(tooltip)
                    .isToday(isToday)
                    .row(dayOfWeekVal)
                    .col(colIndex)
                    .completedHabits(String.join(",", completedHabits))
                    .missedHabits(String.join(",", missedHabits))
                    .build());
        }

        // Dynamically align Month Labels
        List<HeatmapDto.MonthLabel> monthLabels = new ArrayList<>();
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        for (int m = 1; m <= 12; m++) {
            LocalDate firstDayOfMonth = LocalDate.of(year, m, 1);
            int colIndex = (firstDayOfMonth.getDayOfYear() + jan1DayOfWeek - 1) / 7 + 1;
            monthLabels.add(new HeatmapDto.MonthLabel(months[m-1], colIndex));
        }

        return HeatmapDto.builder()
                .year(year)
                .days(days)
                .monthLabels(monthLabels)
                .build();
    }
}
