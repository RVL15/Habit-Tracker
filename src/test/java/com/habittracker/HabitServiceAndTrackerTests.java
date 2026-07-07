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
    private DashboardAnalyticsService analyticsService;

    @Autowired
    private MockMvc mockMvc;

    private User testUser;

    @BeforeEach
    void setUp() {
        habitRepository.deleteAll();
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
}
