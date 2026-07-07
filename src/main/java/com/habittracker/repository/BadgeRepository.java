package com.habittracker.repository;

import com.habittracker.entity.Badge;
import com.habittracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {
    List<Badge> findByUser(User user);
    boolean existsByUserAndBadgeName(User user, String badgeName);
}
