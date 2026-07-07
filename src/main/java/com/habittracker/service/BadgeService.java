package com.habittracker.service;

import com.habittracker.dto.BadgeDto;
import com.habittracker.entity.Badge;
import com.habittracker.entity.Habit;
import com.habittracker.entity.User;
import com.habittracker.repository.BadgeRepository;
import com.habittracker.repository.HabitRepository;
import com.habittracker.repository.HabitTrackerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final HabitRepository habitRepository;
    private final HabitTrackerRepository trackerRepository;
    private final DashboardAnalyticsService analyticsService;

    private static final Map<String, String[]> BADGE_METADATA = new LinkedHashMap<>();
    static {
        // Name -> [Description, Icon]
        BADGE_METADATA.put("First Habit", new String[]{"Create your first habit to begin.", "🌱"});
        BADGE_METADATA.put("First Week", new String[]{"Complete at least one habit this week.", "📅"});
        BADGE_METADATA.put("7 Day Streak", new String[]{"Maintain a 7-day perfect streak.", "🔥"});
        BADGE_METADATA.put("30 Day Streak", new String[]{"Maintain a 30-day perfect streak.", "⚡"});
        BADGE_METADATA.put("100 Completions", new String[]{"Complete habits 100 times in total.", "👑"});
        BADGE_METADATA.put("Perfect Month", new String[]{"Complete all active habits for a full month.", "🏆"});
    }

    @Transactional
    public List<BadgeDto> getBadgesForUser(User user) {
        scanAndUnlockBadges(user);
        
        List<Badge> unlocked = badgeRepository.findByUser(user);
        Map<String, Badge> unlockedMap = new HashMap<>();
        for (Badge b : unlocked) {
            unlockedMap.put(b.getBadgeName(), b);
        }

        List<BadgeDto> list = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

        for (Map.Entry<String, String[]> entry : BADGE_METADATA.entrySet()) {
            String name = entry.getKey();
            String[] meta = entry.getValue();
            Badge badge = unlockedMap.get(name);

            list.add(BadgeDto.builder()
                    .name(name)
                    .description(meta[0])
                    .icon(meta[1])
                    .unlocked(badge != null)
                    .unlockedAt(badge != null ? badge.getUnlockedAt().format(formatter) : "")
                    .build());
        }

        return list;
    }

    private void scanAndUnlockBadges(User user) {
        List<Habit> activeHabits = habitRepository.findByUserAndActiveOrderByDisplayOrderAscIdAsc(user, true);
        if (activeHabits.isEmpty()) return;

        checkAndPersist(user, "First Habit", true);

        long totalCompletions = trackerRepository.findByHabitInAndTrackDateBetween(activeHabits, 
                user.getCreatedAt().toLocalDate(), LocalDate.now(java.time.ZoneId.of("Asia/Kolkata"))).stream()
                .filter(com.habittracker.entity.HabitTracker::isCompleted)
                .count();
        checkAndPersist(user, "100 Completions", totalCompletions >= 100);

        int currentStreak = analyticsService.getDashboardAnalytics(user).getCurrentStreak();
        int longestStreak = analyticsService.getDashboardAnalytics(user).getLongestStreak();
        checkAndPersist(user, "7 Day Streak", longestStreak >= 7);
        checkAndPersist(user, "30 Day Streak", longestStreak >= 30);
        checkAndPersist(user, "First Week", currentStreak >= 1);
        checkAndPersist(user, "Perfect Month", longestStreak >= 30); // simplistic logic for perfect month match
    }

    private void checkAndPersist(User user, String badgeName, boolean condition) {
        if (condition && !badgeRepository.existsByUserAndBadgeName(user, badgeName)) {
            Badge badge = Badge.builder()
                    .user(user)
                    .badgeName(badgeName)
                    .build();
            badgeRepository.save(badge);
        }
    }
}
