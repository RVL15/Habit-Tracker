package com.habittracker.service;

import com.habittracker.entity.User;
import com.habittracker.repository.BadgeRepository;
import com.habittracker.repository.HabitRepository;
import com.habittracker.repository.HabitTrackerRepository;
import com.habittracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final HabitRepository habitRepository;
    private final HabitTrackerRepository habitTrackerRepository;
    private final BadgeRepository badgeRepository;
    private final PasswordEncoder passwordEncoder;

    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalAdmins", userRepository.countByRole("ROLE_ADMIN"));
        stats.put("totalHabits", habitRepository.count());
        stats.put("totalCompletions", habitTrackerRepository.count());
        return stats;
    }

    public List<User> searchUsers(String query, String role) {
        boolean hasQuery = query != null && !query.isBlank();
        boolean hasRole = role != null && !role.isBlank();

        if (hasQuery && hasRole) {
            return userRepository.findByRoleAndNameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(role, query, role, query);
        } else if (hasQuery) {
            return userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
        } else if (hasRole) {
            return userRepository.findByRole(role);
        } else {
            return userRepository.findAll();
        }
    }

    @Transactional
    public void createUser(String name, String email, String password, String role) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered.");
        }
        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role != null ? role : "ROLE_USER")
                .build();
        userRepository.save(user);
    }

    @Transactional
    public void updateUser(Long id, String name, String email, String role, String password) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.getEmail().equalsIgnoreCase(email) && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered by another user.");
        }

        user.setName(name);
        user.setEmail(email);
        user.setRole(role);

        if (password != null && !password.isBlank()) {
            user.setPassword(passwordEncoder.encode(password));
        }

        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // 1. Delete all badges explicitly
        badgeRepository.deleteAll(badgeRepository.findByUser(user));
        
        // 2. Delete all habits explicitly (which cascade deletes trackers)
        List<com.habittracker.entity.Habit> habits = habitRepository.findByUserOrderByDisplayOrderAscIdAsc(user);
        habitRepository.deleteAll(habits);
        
        // 3. Delete user
        userRepository.delete(user);
    }
}
