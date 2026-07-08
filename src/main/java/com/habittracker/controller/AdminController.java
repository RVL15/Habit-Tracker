package com.habittracker.controller;

import com.habittracker.entity.User;
import com.habittracker.service.AdminService;
import com.habittracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    @GetMapping
    public String adminDashboard(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam(required = false) String query,
                                 @RequestParam(required = false) String role,
                                 Model model) {
        User currentUser = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        Map<String, Object> stats = adminService.getSystemStats();
        List<User> users = adminService.searchUsers(query, role);

        model.addAttribute("username", currentUser.getName());
        model.addAttribute("stats", stats);
        model.addAttribute("users", users);
        model.addAttribute("query", query);
        model.addAttribute("role", role);

        return "admin";
    }

    @PostMapping("/users/create")
    public String createUser(@RequestParam String name,
                             @RequestParam String email,
                             @RequestParam String password,
                             @RequestParam String role,
                             RedirectAttributes redirectAttributes) {
        try {
            adminService.createUser(name, email, password, role);
            redirectAttributes.addFlashAttribute("successMessage", "User created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating user: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/users/update")
    public String updateUser(@RequestParam Long id,
                             @RequestParam String name,
                             @RequestParam String email,
                             @RequestParam String role,
                             @RequestParam(required = false) String password,
                             RedirectAttributes redirectAttributes) {
        try {
            adminService.updateUser(id, name, email, role, password);
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating user: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        User currentUser = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        if (currentUser.getId().equals(id)) {
            redirectAttributes.addFlashAttribute("errorMessage", "You cannot delete your own admin account.");
            return "redirect:/admin";
        }
        try {
            adminService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/admin";
    }
}
