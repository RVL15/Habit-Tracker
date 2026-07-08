package com.habittracker.controller;

import com.habittracker.dto.SettingsDto;
import com.habittracker.entity.User;
import com.habittracker.service.SettingsService;
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

@Controller
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;
    private final UserService userService;

    @GetMapping("/settings")
    public String viewSettings(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        SettingsDto settingsDto = settingsService.getSettings(user);
        model.addAttribute("settings", settingsDto);
        model.addAttribute("username", user.getName());
        return "settings";
    }

    @PostMapping("/settings/update")
    public String updateSettings(@AuthenticationPrincipal UserDetails userDetails,
                                 @ModelAttribute("settings") SettingsDto dto,
                                 RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        try {
            settingsService.updateSettings(user, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Settings updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating settings: " + e.getMessage());
        }
        return "redirect:/settings";
    }
}
