package com.habittracker.config;

import com.habittracker.entity.User;
import com.habittracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        User admin = userRepository.findByEmail("admin@example.com").orElse(null);
        if (admin == null) {
            admin = User.builder()
                    .email("admin@example.com")
                    .build();
        }
        admin.setName("System Administrator");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole("ROLE_ADMIN");
        userRepository.save(admin);
        
        System.out.println("====== SEEDED DEFAULT ADMIN ======");
        System.out.println("Email: admin@example.com");
        System.out.println("Password: admin123");
        System.out.println("==================================");
    }
}
