package com.habittracker.service;

import com.habittracker.entity.Habit;
import com.habittracker.entity.HabitTracker;
import com.habittracker.entity.User;
import com.habittracker.repository.HabitRepository;
import com.habittracker.repository.HabitTrackerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final HabitRepository habitRepository;
    private final HabitTrackerRepository trackerRepository;

    public String exportCsv(User user) {
        List<Habit> activeHabits = habitRepository.findByUserAndActiveOrderByDisplayOrderAscIdAsc(user, true);
        if (activeHabits.isEmpty()) {
            return "No data available";
        }

        LocalDate today = LocalDate.now(java.time.ZoneId.of("Asia/Kolkata"));
        LocalDate startOfMonth = today.withDayOfMonth(1);
        List<HabitTracker> trackers = trackerRepository.findByHabitInAndTrackDateBetween(activeHabits, startOfMonth, today);

        StringBuilder csv = new StringBuilder("Date,HabitName,Category,Status\n");
        for (HabitTracker t : trackers) {
            csv.append(t.getTrackDate()).append(",")
               .append(escapeCsv(t.getHabit().getHabitName())).append(",")
               .append(escapeCsv(t.getHabit().getCategory())).append(",")
               .append(t.isCompleted() ? "Completed" : "Missed")
               .append("\n");
        }
        return csv.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
