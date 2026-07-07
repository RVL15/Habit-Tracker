package com.habittracker.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "BADGE", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "badge_name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "badge_name", nullable = false, length = 50)
    private String badgeName;

    @Column(name = "unlocked_at", nullable = false)
    private LocalDateTime unlockedAt;

    @PrePersist
    protected void onCreate() {
        this.unlockedAt = LocalDateTime.now();
    }
}
