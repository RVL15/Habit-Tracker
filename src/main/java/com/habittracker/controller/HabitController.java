package com.habittracker.controller;

import com.habittracker.entity.Habit;
import com.habittracker.entity.User;
import com.habittracker.service.HabitService;
import com.habittracker.service.HabitTrackerService;
import com.habittracker.dto.DashboardAnalyticsDto;
import com.habittracker.service.DashboardAnalyticsService;
import com.habittracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class HabitController {

    private final HabitService habitService;
    private final HabitTrackerService trackerService;
    private final UserService userService;
    private final DashboardAnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails,
                            @RequestParam(required = false) Integer year,
                            @RequestParam(required = false) Integer month,
                            Model model) {
        User user = getAuthenticatedUser(userDetails);
        
        LocalDate now = LocalDate.now(java.time.ZoneId.of("Asia/Kolkata"));
        int selectedYear = (year != null) ? year : now.getYear();
        int selectedMonth = (month != null) ? month : now.getMonthValue();

        LocalDate firstDayOfMonth = LocalDate.of(selectedYear, selectedMonth, 1);
        String monthName = firstDayOfMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        List<Habit> activeHabits = habitService.getActiveUserHabits(user);
        Map<Long, Set<LocalDate>> trackerMap = trackerService.getMonthlyTrackerMap(activeHabits, selectedYear, selectedMonth);

        // Generate list of dates for the selected month
        java.time.YearMonth yearMonth = java.time.YearMonth.of(selectedYear, selectedMonth);
        int totalDays = yearMonth.lengthOfMonth();
        List<LocalDate> monthDates = new ArrayList<>();
        for (int d = 1; d <= totalDays; d++) {
            monthDates.add(yearMonth.atDay(d));
        }

        // Navigation
        LocalDate currentMonthDate = LocalDate.of(selectedYear, selectedMonth, 1);
        LocalDate prevMonthDate = currentMonthDate.minusMonths(1);
        LocalDate nextMonthDate = currentMonthDate.plusMonths(1);

        DashboardAnalyticsDto analytics = analyticsService.getDashboardAnalytics(user);
        model.addAttribute("username", user.getName());
        model.addAttribute("today", now);
        model.addAttribute("habits", activeHabits);
        model.addAttribute("analytics", analytics);
        model.addAttribute("trackerMap", trackerMap);
        model.addAttribute("monthDates", monthDates);
        model.addAttribute("currentYear", selectedYear);
        model.addAttribute("currentMonth", selectedMonth);
        model.addAttribute("monthName", monthName);
        
        model.addAttribute("prevYear", prevMonthDate.getYear());
        model.addAttribute("prevMonth", prevMonthDate.getMonthValue());
        model.addAttribute("nextYear", nextMonthDate.getYear());
        model.addAttribute("nextMonth", nextMonthDate.getMonthValue());

        return "index";
    }

    @GetMapping("/habits")
    public String listHabits(@AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam(required = false) String search,
                             @RequestParam(required = false) String status,
                             @RequestParam(required = false) String category,
                             @RequestParam(required = false) String sort,
                             Model model) {
        User user = getAuthenticatedUser(userDetails);
        List<Habit> habits = habitService.getUserHabits(user);

        // Apply Search Filter (matches name or description)
        if (search != null && !search.trim().isEmpty()) {
            String query = search.trim().toLowerCase();
            habits = habits.stream()
                    .filter(h -> h.getHabitName().toLowerCase().contains(query) ||
                                 (h.getDescription() != null && h.getDescription().toLowerCase().contains(query)))
                    .collect(java.util.stream.Collectors.toList());
        }

        // Apply Category Filter
        if (category != null && !category.trim().isEmpty() && !"All".equalsIgnoreCase(category)) {
            String cat = category.trim().toLowerCase();
            habits = habits.stream()
                    .filter(h -> h.getCategory() != null && h.getCategory().toLowerCase().equals(cat))
                    .collect(java.util.stream.Collectors.toList());
        }

        // Apply Status Filter
        if (status != null && !status.trim().isEmpty() && !"All".equalsIgnoreCase(status)) {
            if ("Active".equalsIgnoreCase(status)) {
                habits = habits.stream().filter(Habit::isActive).collect(java.util.stream.Collectors.toList());
            } else if ("Inactive".equalsIgnoreCase(status)) {
                habits = habits.stream().filter(h -> !h.isActive()).collect(java.util.stream.Collectors.toList());
            } else if ("Completed".equalsIgnoreCase(status)) {
                LocalDate today = LocalDate.now(java.time.ZoneId.of("Asia/Kolkata"));
                habits = habits.stream()
                        .filter(h -> h.getTrackers().stream()
                                .anyMatch(t -> t.getTrackDate().equals(today) && t.isCompleted()))
                        .collect(java.util.stream.Collectors.toList());
            } else if ("Pending".equalsIgnoreCase(status)) {
                LocalDate today = LocalDate.now(java.time.ZoneId.of("Asia/Kolkata"));
                habits = habits.stream()
                        .filter(h -> h.getTrackers().stream()
                                .noneMatch(t -> t.getTrackDate().equals(today) && t.isCompleted()))
                        .collect(java.util.stream.Collectors.toList());
            }
        }

        // Apply Sorting
        if (sort != null && !sort.trim().isEmpty()) {
            if ("Alphabetical".equalsIgnoreCase(sort)) {
                habits.sort(Comparator.comparing(h -> h.getHabitName().toLowerCase()));
            } else if ("Newest".equalsIgnoreCase(sort)) {
                habits.sort(Comparator.comparing(Habit::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
            } else if ("Oldest".equalsIgnoreCase(sort)) {
                habits.sort(Comparator.comparing(Habit::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())));
            } else if ("Completion".equalsIgnoreCase(sort)) {
                habits.sort((h1, h2) -> {
                    long c1 = h1.getTrackers().stream().filter(com.habittracker.entity.HabitTracker::isCompleted).count();
                    long c2 = h2.getTrackers().stream().filter(com.habittracker.entity.HabitTracker::isCompleted).count();
                    return Long.compare(c2, c1); // descending completion order
                });
            }
        }

        model.addAttribute("username", user.getName());
        model.addAttribute("habits", habits);
        
        model.addAttribute("selectedSearch", search);
        model.addAttribute("selectedStatus", status != null ? status : "All");
        model.addAttribute("selectedCategory", category != null ? category : "All");
        model.addAttribute("selectedSort", sort != null ? sort : "Alphabetical");

        return "habits";
    }

    @GetMapping("/habit/add")
    public String showAddForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getAuthenticatedUser(userDetails);
        model.addAttribute("username", user.getName());
        model.addAttribute("habit", new Habit());
        return "habit-form";
    }

    @PostMapping("/habit/add")
    public String addHabit(@AuthenticationPrincipal UserDetails userDetails,
                           @ModelAttribute("habit") Habit habit,
                           BindingResult result,
                           Model model) {
        User user = getAuthenticatedUser(userDetails);
        validateHabit(habit, result, user, null);

        if (result.hasErrors()) {
            model.addAttribute("username", user.getName());
            return "habit-form";
        }

        habitService.createHabit(habit, user);
        return "redirect:/habits";
    }

    @GetMapping("/habit/edit/{id}")
    public String showEditForm(@AuthenticationPrincipal UserDetails userDetails,
                               @PathVariable Long id,
                               Model model) {
        User user = getAuthenticatedUser(userDetails);
        Habit habit = habitService.getHabitById(id)
                .orElseThrow(() -> new IllegalArgumentException("Habit not found"));

        if (!habit.getUser().getId().equals(user.getId())) {
            return "redirect:/habits";
        }

        model.addAttribute("username", user.getName());
        model.addAttribute("habit", habit);
        return "habit-form";
    }

    @PostMapping("/habit/edit/{id}")
    public String editHabit(@AuthenticationPrincipal UserDetails userDetails,
                            @PathVariable Long id,
                            @ModelAttribute("habit") Habit habit,
                            BindingResult result,
                            Model model) {
        User user = getAuthenticatedUser(userDetails);
        validateHabit(habit, result, user, id);

        if (result.hasErrors()) {
            model.addAttribute("username", user.getName());
            return "habit-form";
        }

        habitService.updateHabit(id, habit, user);
        return "redirect:/habits";
    }

    @PostMapping("/habit/delete/{id}")
    public String deleteHabit(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        User user = getAuthenticatedUser(userDetails);
        habitService.deleteHabit(id, user);
        return "redirect:/habits";
    }

    @PostMapping("/habit/toggle-active/{id}")
    public String toggleActive(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        User user = getAuthenticatedUser(userDetails);
        habitService.toggleActive(id, user);
        return "redirect:/habits";
    }

    // AJAX API endpoints
    @PostMapping("/habit/check")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkHabit(@AuthenticationPrincipal UserDetails userDetails,
                                                          @RequestParam Long habitId,
                                                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        User user = getAuthenticatedUser(userDetails);
        LocalDate today = LocalDate.now(java.time.ZoneId.of("Asia/Kolkata"));
        if (!date.equals(today)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Only today's date can be edited");
            return ResponseEntity.badRequest().body(response);
        }
        trackerService.trackHabit(habitId, date, true, user);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Habit checked");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/habit/uncheck")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uncheckHabit(@AuthenticationPrincipal UserDetails userDetails,
                                                            @RequestParam Long habitId,
                                                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        User user = getAuthenticatedUser(userDetails);
        LocalDate today = LocalDate.now(java.time.ZoneId.of("Asia/Kolkata"));
        if (!date.equals(today)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Only today's date can be edited");
            return ResponseEntity.badRequest().body(response);
        }
        trackerService.trackHabit(habitId, date, false, user);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Habit unchecked");
        return ResponseEntity.ok(response);
    }

    private User getAuthenticatedUser(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not authenticated"));
    }

    private void validateHabit(Habit habit, BindingResult result, User user, Long habitId) {
        if (habit.getHabitName() == null || habit.getHabitName().trim().isEmpty()) {
            result.rejectValue("habitName", "error.habit", "Habit name is required");
            return;
        }

        String name = habit.getHabitName().trim();
        if (name.length() > 50) {
            result.rejectValue("habitName", "error.habit", "Habit name must not exceed 50 characters");
            return;
        }

        boolean isDuplicate = (habitId == null)
                ? habitService.existsByName(user, name)
                : habitService.existsByNameAndNotId(user, name, habitId);

        if (isDuplicate) {
            result.rejectValue("habitName", "error.habit", "You already have a habit with this name");
        }
    }
}
