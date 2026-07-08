package com.habittracker.controller;

import com.habittracker.entity.User;
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

@Controller
@RequiredArgsConstructor
public class AnalyticsController {

    private final UserService userService;
    private final HeatmapService heatmapService;
    private final InsightService insightService;
    private final GoalService goalService;
    private final ReportService reportService;
    private final HistoryService historyService;

    @GetMapping("/analytics")
    public String viewAnalytics(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam(value = "range", defaultValue = "week") String range,
                                Model model) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        ZoneId zone = ZoneId.of(user.getTimezone() != null ? user.getTimezone() : "Asia/Kolkata");
        int currentYear = LocalDate.now(zone).getYear();

        var heatmap = heatmapService.getHeatmap(user, currentYear);

        model.addAttribute("heatmap", heatmap);
        model.addAttribute("insights", insightService.getInsights(user));
        model.addAttribute("goals", goalService.getGoals(user));
        model.addAttribute("report", reportService.getMonthlyReport(user));
        model.addAttribute("history", historyService.getHistory(user, range));
        model.addAttribute("selectedRange", range);

        return "analytics";
    }
}
