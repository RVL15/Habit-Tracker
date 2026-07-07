package com.habittracker.service;

import com.habittracker.dto.ProfileDto;
import com.habittracker.entity.User;
import com.habittracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void updateProfile(User user, ProfileDto dto) {
        user.setName(dto.getName());
        user.setTimezone(dto.getTimezone());
        user.setDailyGoal(dto.getDailyGoal());
        user.setTheme(dto.getTheme());
        user.setNotificationsEnabled(dto.isNotificationsEnabled());
        user.setProfilePicture(dto.getProfilePicture());

        if (dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {
            validateAndChangePassword(user, dto);
        }

        userRepository.save(user);
    }

    private void validateAndChangePassword(User user, ProfileDto dto) {
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password does not match.");
        }
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("New passwords do not match.");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
    }
}
