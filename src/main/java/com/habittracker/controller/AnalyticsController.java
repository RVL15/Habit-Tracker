package com.habittracker.controller;

import com.habittracker.entity.Habit;
import com.habittracker.entity.HabitTracker;
import com.habittracker.entity.User;
import com.habittracker.repository.HabitRepository;
import com.habittracker.repository.HabitTrackerRepository;
import com.habittracker.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class AnalyticsController {

    private final UserService userService;
    private final HabitRepository habitRepository;
    private final HabitTrackerRepository trackerRepository;
    private final InsightService insightService;
    private final GoalService goalService;
    private final ReportService reportService;
    private final HistoryService historyService;

    public record HeatmapCellDto(String date, int completed, int percent, String intensityClass, String tooltip) {}

    @GetMapping("/analytics")
    public String viewAnalytics(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam(value = "range", defaultValue = "week") String range,
                                Model model) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        List<Habit> activeHabits = habitRepository.findByUserAndActiveOrderByDisplayOrderAscIdAsc(user, true);

        // Compute 365 Days Heatmap cells
        List<HeatmapCellDto> heatmapCells = calculateHeatmap(user, activeHabits);

        model.addAttribute("heatmapCells", heatmapCells);
        model.addAttribute("insights", insightService.getInsights(user));
        model.addAttribute("goals", goalService.getGoals(user));
        model.addAttribute("report", reportService.getMonthlyReport(user));
        model.addAttribute("history", historyService.getHistory(user, range));
        model.addAttribute("selectedRange", range);

        return "analytics";
    }

    private List<HeatmapCellDto> calculateHeatmap(User user, List<Habit> activeHabits) {
        ZoneId zone = ZoneId.of(user.getTimezone() != null ? user.getTimezone() : "Asia/Kolkata");
        LocalDate today = LocalDate.now(zone);
        LocalDate startDate = today.minusDays(364);

        List<HeatmapCellDto> heatmapCells = new ArrayList<>();
        if (activeHabits.isEmpty()) {
            return heatmapCells;
        }

        List<HabitTracker> trackers = trackerRepository.findByHabitInAndTrackDateBetween(activeHabits, startDate, today);
        Map<LocalDate, List<HabitTracker>> grouped = trackers.stream()
                .filter(HabitTracker::isCompleted)
                .collect(Collectors.groupingBy(HabitTracker::getTrackDate));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy");

        for (LocalDate date = startDate; !date.isAfter(today); date = date.plusDays(1)) {
            int completed = grouped.getOrDefault(date, Collections.emptyList()).size();
            int total = activeHabits.size();
            int percent = (completed * 100) / total;

            String intensity = "heatmap-level-0";
            if (completed > 0) {
                if (completed == total) {
                    intensity = "heatmap-level-4";
                } else if (percent >= 70) {
                    intensity = "heatmap-level-3";
                } else if (percent >= 30) {
                    intensity = "heatmap-level-2";
                } else {
                    intensity = "heatmap-level-1";
                }
            }

            String tooltip = date.format(dtf) + ": " + completed + "/" + total + " completed (" + percent + "%)";
            heatmapCells.add(new HeatmapCellDto(date.toString(), completed, percent, intensity, tooltip));
        }

        return heatmapCells;
    }
}
