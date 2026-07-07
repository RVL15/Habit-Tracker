package com.habittracker.service;

import com.habittracker.dto.HistoryDto;
import com.habittracker.dto.HistoryDto.HabitStatus;
import com.habittracker.entity.Habit;
import com.habittracker.entity.HabitTracker;
import com.habittracker.entity.User;
import com.habittracker.repository.HabitRepository;
import com.habittracker.repository.HabitTrackerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final HabitRepository habitRepository;
    private final HabitTrackerRepository trackerRepository;

    public List<HistoryDto> getHistory(User user, String range) {
        List<Habit> activeHabits = habitRepository.findByUserAndActiveOrderByDisplayOrderAscIdAsc(user, true);
        if (activeHabits.isEmpty()) {
            return Collections.emptyList();
        }

        ZoneId zone = ZoneId.of(user.getTimezone() != null ? user.getTimezone() : "Asia/Kolkata");
        LocalDate today = LocalDate.now(zone);
        int days = getRangeDays(range);

        LocalDate startDate = today.minusDays(days - 1);
        List<HabitTracker> trackers = trackerRepository.findByHabitInAndTrackDateBetween(activeHabits, startDate, today);
        
        Map<LocalDate, Map<Long, HabitTracker>> trackersByDateAndHabit = trackers.stream()
                .collect(Collectors.groupingBy(
                        HabitTracker::getTrackDate,
                        Collectors.toMap(t -> t.getHabit().getId(), t -> t)
                ));

        List<HistoryDto> history = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

        for (int i = 0; i < days; i++) {
            LocalDate date = today.minusDays(i);
            if (date.isBefore(user.getCreatedAt().toLocalDate())) {
                break; // Stop at user registration date
            }

            Map<Long, HabitTracker> dayMap = trackersByDateAndHabit.getOrDefault(date, Collections.emptyMap());
            List<HabitStatus> statuses = activeHabits.stream()
                    .map(h -> {
                        HabitTracker t = dayMap.get(h.getId());
                        boolean completed = (t != null && t.isCompleted());
                        return new HabitStatus(h.getHabitName(), completed, h.getColor());
                    })
                    .collect(Collectors.toList());

            history.add(new HistoryDto(date.format(formatter), statuses));
        }

        return history;
    }

    private int getRangeDays(String range) {
        if ("month".equalsIgnoreCase(range)) {
            return 30;
        } else if ("year".equalsIgnoreCase(range)) {
            return 365;
        }
        return 7; // Default "week"
    }
}
