package com.habittracker;

import com.habittracker.entity.Habit;
import com.habittracker.entity.User;
import com.habittracker.repository.HabitRepository;
import com.habittracker.repository.UserRepository;
import com.habittracker.service.HabitService;
import com.habittracker.service.HabitTrackerService;
import com.habittracker.service.DashboardAnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class HabitServiceAndTrackerTests {

    @Autowired
    private HabitService habitService;

    @Autowired
    private HabitTrackerService trackerService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private com.habittracker.repository.BadgeRepository badgeRepository;

    @Autowired
    private DashboardAnalyticsService analyticsService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.habittracker.service.SettingsService settingsService;

    @Autowired
    private com.habittracker.service.ExportService exportService;

    @Autowired
    private com.habittracker.service.HeatmapService heatmapService;

    @Autowired
    private com.habittracker.service.AdminService adminService;

    private User testUser;

    @BeforeEach
    void setUp() {
        habitRepository.deleteAll();
        badgeRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .name("Alice Smith")
                .email("alice@example.com")
                .password("encodedPassword123")
                .build();
        testUser = userRepository.save(testUser);
    }

    @Test
    void testCreateHabitAndDuplicateValidation() {
        Habit habit1 = Habit.builder()
                .habitName("Exercise Daily")
                .description("Morning gym session")
                .color("#2ecc71")
                .displayOrder(1)
                .build();

        Habit saved = habitService.createHabit(habit1, testUser);
        assertNotNull(saved.getId());
        assertEquals("Exercise Daily", saved.getHabitName());

        // Test duplicate validation
        Habit duplicateHabit = Habit.builder()
                .habitName("  EXERCISE DAILY  ") // check trimming and case insensitivity
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            habitService.createHabit(duplicateHabit, testUser);
        });
    }

    @Test
    void testUpdateAndDeleteHabit() {
        Habit habit = Habit.builder()
                .habitName("Read Books")
                .displayOrder(5)
                .build();
        habit = habitService.createHabit(habit, testUser);

        Habit updatedDetails = Habit.builder()
                .habitName("Read 10 Pages")
                .description("Self improvement")
                .color("#3498db")
                .displayOrder(2)
                .build();

        Habit updated = habitService.updateHabit(habit.getId(), updatedDetails, testUser);
        assertEquals("Read 10 Pages", updated.getHabitName());
        assertEquals("#3498db", updated.getColor());

        habitService.deleteHabit(updated.getId(), testUser);
        assertTrue(habitService.getHabitById(updated.getId()).isEmpty());
    }

    @Test
    void testTrackHabitCheckboxSaveAndReload() {
        Habit habit = Habit.builder()
                .habitName("Drink Water")
                .displayOrder(1)
                .build();
        habit = habitService.createHabit(habit, testUser);

        LocalDate today = LocalDate.now(java.time.ZoneId.of("Asia/Kolkata"));

        // Check/track
        trackerService.trackHabit(habit.getId(), today, true, testUser);

        // Fetch monthly grid records
        List<Habit> habits = List.of(habit);
        Map<Long, Set<LocalDate>> monthlyMap = trackerService.getMonthlyTrackerMap(habits, today.getYear(), today.getMonthValue());

        assertTrue(monthlyMap.containsKey(habit.getId()));
        assertTrue(monthlyMap.get(habit.getId()).contains(today));

        // Uncheck
        trackerService.trackHabit(habit.getId(), today, false, testUser);
        monthlyMap = trackerService.getMonthlyTrackerMap(habits, today.getYear(), today.getMonthValue());
        assertFalse(monthlyMap.containsKey(habit.getId()) && monthlyMap.get(habit.getId()).contains(today));
    }

    @Test
    void testDashboardAnalyticsCalculation() {
        Habit habit1 = Habit.builder()
                .habitName("Exercise")
                .displayOrder(1)
                .build();
        habit1 = habitService.createHabit(habit1, testUser);

        Habit habit2 = Habit.builder()
                .habitName("Read")
                .displayOrder(2)
                .build();
        habit2 = habitService.createHabit(habit2, testUser);

        LocalDate today = LocalDate.now(java.time.ZoneId.of("Asia/Kolkata"));

        // Habit 1 is completed today
        trackerService.trackHabit(habit1.getId(), today, true, testUser);

        var analytics = analyticsService.getDashboardAnalytics(testUser);
        assertEquals(2, analytics.getTotalHabits());
        assertEquals(1, analytics.getCompletedToday());
        assertEquals(1, analytics.getPendingToday());
        assertEquals(50, analytics.getTodayCompletion());
        assertEquals("Exercise", analytics.getBestHabit());
        assertEquals("Read", analytics.getWorstHabit());
    }

    @Test
    void testDashboardControllerError() throws Exception {
        Habit habit = Habit.builder()
                .habitName("Exercise")
                .displayOrder(1)
                .build();
        habitService.createHabit(habit, testUser);

        mockMvc.perform(get("/dashboard")
                .with(user(testUser.getEmail()).roles("USER")))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print());
    }

    @Test
    void testSettingsServiceSaveAndLoad() {
        var settingsDto = com.habittracker.dto.SettingsDto.builder()
                .theme("dark")
                .language("es")
                .timezone("America/New_York")
                .startDay("Sunday")
                .weekFormat("Sun-Sat")
                .notificationTime("09:30")
                .notificationsEnabled(false)
                .build();

        settingsService.updateSettings(testUser, settingsDto);

        var loaded = settingsService.getSettings(testUser);
        assertEquals("dark", loaded.getTheme());
        assertEquals("es", loaded.getLanguage());
        assertEquals("America/New_York", loaded.getTimezone());
        assertEquals("Sunday", loaded.getStartDay());
        assertEquals("Sun-Sat", loaded.getWeekFormat());
        assertEquals("09:30", loaded.getNotificationTime());
        assertFalse(loaded.isNotificationsEnabled());
    }

    @Test
    void testExcelExportGeneratesBytes() {
        Habit habit = Habit.builder()
                .habitName("Exercise")
                .displayOrder(1)
                .category("Fitness")
                .build();
        habitService.createHabit(habit, testUser);

        byte[] excelBytes = exportService.exportExcel(testUser);
        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0);
    }

    @Test
    void testHeatmapGeneration() {
        Habit habit = Habit.builder()
                .habitName("Exercise")
                .displayOrder(1)
                .category("Fitness")
                .build();
        habitService.createHabit(habit, testUser);

        var heatmap = heatmapService.getHeatmap(testUser, 2026);
        assertNotNull(heatmap);
        assertEquals(2026, heatmap.getYear());
        // 2026 is not a leap year, so should have 365 days
        assertEquals(365, heatmap.getDays().size());
        assertEquals(12, heatmap.getMonthLabels().size());

        // Assert details of Jan 1st 2026
        var firstDay = heatmap.getDays().get(0);
        assertEquals("2026-01-01", firstDay.getDate());
        // Jan 1 2026 was a Thursday. Thursday % 7 + 1 = 4 % 7 + 1 = 5
        assertEquals(5, firstDay.getRow());
        assertEquals(1, firstDay.getCol());
    }

    @Test
    void testAdminSecurityAccess() throws Exception {
        // User with ROLE_USER should receive 403 Forbidden
        mockMvc.perform(get("/admin")
                .with(user(testUser.getEmail()).roles("USER")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());

        // User with ROLE_ADMIN should receive 200 OK
        mockMvc.perform(get("/admin")
                .with(user(testUser.getEmail()).authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }

    @Test
    void testAdminUserCrudAndCascade() {
        // Create user
        adminService.createUser("Test Admin CRUD", "admincrud@example.com", "pass123", "ROLE_USER");
        User created = userRepository.findByEmail("admincrud@example.com").orElseThrow();
        assertEquals("Test Admin CRUD", created.getName());
        assertEquals("ROLE_USER", created.getRole());

        // Update user
        adminService.updateUser(created.getId(), "Updated Admin CRUD", "admincrud@example.com", "ROLE_ADMIN", "newpass123");
        User updated = userRepository.findById(created.getId()).orElseThrow();
        assertEquals("Updated Admin CRUD", updated.getName());
        assertEquals("ROLE_ADMIN", updated.getRole());

        // Create a habit and tracker log for updated user to test cascade delete
        Habit habit = Habit.builder()
                .habitName("Temp Habit")
                .displayOrder(1)
                .build();
        habitService.createHabit(habit, updated);
        assertFalse(habitRepository.findByUserOrderByDisplayOrderAscIdAsc(updated).isEmpty());

        // Delete user
        adminService.deleteUser(updated.getId());
        assertTrue(userRepository.findByEmail("admincrud@example.com").isEmpty());
        // Verify cascade delete on habits
        assertTrue(habitRepository.findByUserOrderByDisplayOrderAscIdAsc(updated).isEmpty());
    }
}
