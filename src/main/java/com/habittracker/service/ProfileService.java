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

    @Transactional
    public void uploadProfilePicture(User user, org.springframework.web.multipart.MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty.");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image uploads are permitted.");
        }

        try {
            java.nio.file.Path uploadDir = java.nio.file.Paths.get("uploads");
            if (!java.nio.file.Files.exists(uploadDir)) {
                java.nio.file.Files.createDirectories(uploadDir);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = ".png";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String filename = "profile-" + user.getId() + extension;
            java.nio.file.Path filePath = uploadDir.resolve(filename);
            
            java.nio.file.Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            user.setProfilePicture("/uploads/" + filename);
            userRepository.save(user);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to save profile picture: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deleteProfilePicture(User user) {
        String currentPic = user.getProfilePicture();
        if (currentPic != null && currentPic.startsWith("/uploads/")) {
            try {
                String filename = currentPic.substring(currentPic.lastIndexOf("/") + 1);
                java.nio.file.Path filePath = java.nio.file.Paths.get("uploads").resolve(filename);
                java.nio.file.Files.deleteIfExists(filePath);
            } catch (java.io.IOException e) {
                // Ignore file system errors
            }
        }
        
        user.setProfilePicture("👤");
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
