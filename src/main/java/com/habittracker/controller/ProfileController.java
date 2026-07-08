package com.habittracker.controller;

import com.habittracker.dto.ProfileDto;
import com.habittracker.entity.User;
import com.habittracker.service.BadgeService;
import com.habittracker.service.DashboardAnalyticsService;
import com.habittracker.service.ProfileService;
import com.habittracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.format.DateTimeFormatter;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final BadgeService badgeService;
    private final UserService userService;
    private final DashboardAnalyticsService analyticsService;

    @GetMapping("/profile")
    public String viewProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        
        ProfileDto dto = ProfileDto.builder()
                .name(user.getName())
                .email(user.getEmail())
                .timezone(user.getTimezone() != null ? user.getTimezone() : "Asia/Kolkata")
                .dailyGoal(user.getDailyGoal() != null ? user.getDailyGoal() : 3)
                .theme(user.getTheme() != null ? user.getTheme() : "light")
                .notificationsEnabled(user.isNotificationsEnabled())
                .profilePicture(user.getProfilePicture() != null ? user.getProfilePicture() : "👤")
                .build();

        var analytics = analyticsService.getDashboardAnalytics(user);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

        model.addAttribute("profile", dto);
        model.addAttribute("badges", badgeService.getBadgesForUser(user));
        model.addAttribute("analytics", analytics);
        model.addAttribute("memberSince", user.getCreatedAt().format(formatter));
        model.addAttribute("level", calculateLevel(analytics.getTotalHabits(), analytics.getOverallCompletion()));

        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @ModelAttribute("profile") ProfileDto dto,
                                RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        try {
            profileService.updateProfile(user, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/profile/upload")
    public String uploadProfilePicture(@AuthenticationPrincipal UserDetails userDetails,
                                       @org.springframework.web.bind.annotation.RequestParam("profileImage") org.springframework.web.multipart.MultipartFile file,
                                       RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        try {
            profileService.uploadProfilePicture(user, file);
            redirectAttributes.addFlashAttribute("successMessage", "Profile picture uploaded successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Upload failed: " + e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/profile/delete-picture")
    public String deleteProfilePicture(@AuthenticationPrincipal UserDetails userDetails,
                                       RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        try {
            profileService.deleteProfilePicture(user);
            redirectAttributes.addFlashAttribute("successMessage", "Profile picture reset to default icon!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Deletion failed: " + e.getMessage());
        }
        return "redirect:/profile";
    }

    private int calculateLevel(int totalHabits, int completionPercent) {
        // Simple levels formulas: level 1 is base, level incremented for every 10 completions
        return 1 + (totalHabits * completionPercent) / 100;
    }
}
