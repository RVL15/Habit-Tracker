package com.habittracker.service;

import com.habittracker.entity.Habit;
import com.habittracker.entity.HabitTracker;
import com.habittracker.entity.User;
import com.habittracker.repository.HabitRepository;
import com.habittracker.repository.HabitTrackerRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
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

    public byte[] exportExcel(User user) {
        List<Habit> activeHabits = habitRepository.findByUserAndActiveOrderByDisplayOrderAscIdAsc(user, true);
        if (activeHabits.isEmpty()) {
            return new byte[0];
        }

        LocalDate today = LocalDate.now(java.time.ZoneId.of("Asia/Kolkata"));
        LocalDate startOfMonth = today.withDayOfMonth(1);
        List<HabitTracker> trackers = trackerRepository.findByHabitInAndTrackDateBetween(activeHabits, startOfMonth, today);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Habit Tracker History");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // Create headers
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Date", "Habit Name", "Category", "Description", "Status"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Write tracker rows
            int rowIdx = 1;
            for (HabitTracker t : trackers) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(t.getTrackDate().toString());
                row.createCell(1).setCellValue(t.getHabit().getHabitName());
                row.createCell(2).setCellValue(t.getHabit().getCategory());
                row.createCell(3).setCellValue(t.getHabit().getDescription() != null ? t.getHabit().getDescription() : "");
                row.createCell(4).setCellValue(t.isCompleted() ? "Completed" : "Missed");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export Excel report", e);
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
