package com.habittracker.controller;

import com.habittracker.entity.User;
import com.habittracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UserService userService;

    @ModelAttribute("currentUserTheme")
    public String getCurrentUserTheme(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            return userService.findByEmail(userDetails.getUsername())
                    .map(User::getTheme)
                    .orElse("light");
        }
        return "light";
    }

    @ModelAttribute("currentUserProfilePicture")
    public String getCurrentUserProfilePicture(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            return userService.findByEmail(userDetails.getUsername())
                    .map(User::getProfilePicture)
                    .orElse("👤");
        }
        return "👤";
    }
}
