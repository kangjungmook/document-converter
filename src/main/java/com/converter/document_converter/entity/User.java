package com.converter.document_converter.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_api_key", columnList = "api_key")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.FREE;

    @Column(name = "api_key", unique = true, length = 64)
    private String apiKey;

    @Column(name = "daily_usage")
    @Builder.Default
    private Integer dailyUsage = 0;

    @Column(name = "monthly_usage")
    @Builder.Default
    private Integer monthlyUsage = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "subscription_end_date")
    private LocalDateTime subscriptionEndDate;

    @Column(name = "last_reset_date")
    @Builder.Default
    private LocalDateTime lastResetDate = LocalDateTime.now();

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    public enum UserRole {
        FREE, PRO, ENTERPRISE
    }

    public boolean canUseService() {
        if (!isActive) return false;
        if (role == UserRole.FREE) {
            return dailyUsage < 10;
        }
        return true;
    }

    public long getFileSizeLimit() {
        return switch (role) {
            case FREE -> 5 * 1024 * 1024;
            case PRO -> 50 * 1024 * 1024;
            case ENTERPRISE -> 500 * 1024 * 1024;
        };
    }

    public void incrementUsage() {
        this.dailyUsage++;
        this.monthlyUsage++;
    }
}