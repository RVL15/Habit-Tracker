package com.habittracker.service;

import com.habittracker.dto.SettingsDto;
import com.habittracker.entity.User;
import com.habittracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final UserRepository userRepository;

    public SettingsDto getSettings(User user) {
        return SettingsDto.builder()
                .theme(user.getTheme() != null ? user.getTheme() : "light")
                .language(user.getLanguage() != null ? user.getLanguage() : "en")
                .timezone(user.getTimezone() != null ? user.getTimezone() : "Asia/Kolkata")
                .startDay(user.getStartDay() != null ? user.getStartDay() : "Monday")
                .weekFormat(user.getWeekFormat() != null ? user.getWeekFormat() : "Mon-Sun")
                .notificationTime(user.getNotificationTime() != null ? user.getNotificationTime() : "08:00")
                .notificationsEnabled(user.isNotificationsEnabled())
                .build();
    }

    @Transactional
    public void updateSettings(User user, SettingsDto dto) {
        user.setTheme(dto.getTheme());
        user.setLanguage(dto.getLanguage());
        user.setTimezone(dto.getTimezone());
        user.setStartDay(dto.getStartDay());
        user.setWeekFormat(dto.getWeekFormat());
        user.setNotificationTime(dto.getNotificationTime());
        user.setNotificationsEnabled(dto.isNotificationsEnabled());
        userRepository.save(user);
    }
}
