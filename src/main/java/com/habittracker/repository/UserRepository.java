package com.habittracker.repository;

import com.habittracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByRole(String role);
    java.util.List<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);
    java.util.List<User> findByRole(String role);
    java.util.List<User> findByRoleAndNameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(String role1, String name, String role2, String email);
}
