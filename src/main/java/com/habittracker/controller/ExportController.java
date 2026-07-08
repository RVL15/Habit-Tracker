package com.habittracker.controller;

import com.habittracker.entity.User;
import com.habittracker.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequiredArgsConstructor
public class ExportController {

    private final UserService userService;
    private final ExportService exportService;
    private final ReportService reportService;
    private final GoalService goalService;
    private final HabitService habitService;
    private final DashboardAnalyticsService analyticsService;

    @GetMapping("/exports/csv")
    public ResponseEntity<byte[]> downloadCsv(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        String csvData = exportService.exportCsv(user);
        byte[] bytes = csvData.getBytes();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=habit_history.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    @GetMapping("/exports/pdf")
    public String viewPdfReport(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        
        var report = reportService.getMonthlyReport(user);
        var analytics = analyticsService.getDashboardAnalytics(user);
        var habits = habitService.getActiveUserHabits(user);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");

        model.addAttribute("report", report);
        model.addAttribute("analytics", analytics);
        model.addAttribute("habits", habits);
        model.addAttribute("user", user);
        model.addAttribute("generatedDate", LocalDateTime.now().format(formatter));

        return "pdf-report";
    }

    @GetMapping("/exports/excel")
    public ResponseEntity<byte[]> downloadExcel(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        byte[] bytes = exportService.exportExcel(user);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=habit_history.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }
}
