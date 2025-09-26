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

    // ===== 전체 사용량 =====
    @Column(name = "daily_usage")
    @Builder.Default
    private Integer dailyUsage = 0;

    @Column(name = "monthly_usage")
    @Builder.Default
    private Integer monthlyUsage = 0;

    // ===== 기능별 사용량 =====
    @Column(name = "pdf_merge_usage")
    @Builder.Default
    private Integer pdfMergeUsage = 0;

    @Column(name = "pdf_split_usage")
    @Builder.Default
    private Integer pdfSplitUsage = 0;

    @Column(name = "image_resize_usage")
    @Builder.Default
    private Integer imageResizeUsage = 0;

    @Column(name = "image_compress_usage")
    @Builder.Default
    private Integer imageCompressUsage = 0;

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

    // ===== 사용자 등급 =====
    public enum UserRole {
        FREE,           // 하루 10회
        PRO,            // 하루 100회
        ENTERPRISE      // 무제한
    }

    // ===== 기능 타입 =====
    public enum FeatureType {
        PDF_MERGE,
        PDF_SPLIT,
        IMAGE_RESIZE,
        IMAGE_COMPRESS
    }

    // ===== 전체 사용 가능 여부 =====
    public boolean canUseService() {
        if (!isActive) return false;

        return switch (role) {
            case FREE -> dailyUsage < 10;
            case PRO -> dailyUsage < 100;
            case ENTERPRISE -> true;
        };
    }

    // ===== 기능별 사용 가능 여부 =====
    public boolean canUseFeature(FeatureType feature) {
        if (!isActive) return false;
        if (role == UserRole.ENTERPRISE) return true;

        int limit = role == UserRole.FREE ? 10 : 100;

        return switch (feature) {
            case PDF_MERGE -> pdfMergeUsage < limit;
            case PDF_SPLIT -> pdfSplitUsage < limit;
            case IMAGE_RESIZE -> imageResizeUsage < limit;
            case IMAGE_COMPRESS -> imageCompressUsage < limit;
        };
    }

    // ===== 파일 크기 제한 =====
    public long getFileSizeLimit() {
        return switch (role) {
            case FREE -> 5 * 1024 * 1024;       // 5MB
            case PRO -> 50 * 1024 * 1024;       // 50MB
            case ENTERPRISE -> 500 * 1024 * 1024; // 500MB
        };
    }

    // ===== 사용량 증가 (전체) =====
    public void incrementUsage() {
        this.dailyUsage++;
        this.monthlyUsage++;
    }

    // ===== 기능별 사용량 증가 =====
    public void incrementFeatureUsage(FeatureType feature) {
        this.dailyUsage++;
        this.monthlyUsage++;

        switch (feature) {
            case PDF_MERGE -> this.pdfMergeUsage++;
            case PDF_SPLIT -> this.pdfSplitUsage++;
            case IMAGE_RESIZE -> this.imageResizeUsage++;
            case IMAGE_COMPRESS -> this.imageCompressUsage++;
        }
    }

    // ===== 일일 사용량 초기화 =====
    public void resetDailyUsage() {
        this.dailyUsage = 0;
        this.pdfMergeUsage = 0;
        this.pdfSplitUsage = 0;
        this.imageResizeUsage = 0;
        this.imageCompressUsage = 0;
        this.lastResetDate = LocalDateTime.now();
    }

    // ===== 남은 사용 횟수 =====
    public int getRemainingUsage() {
        return switch (role) {
            case FREE -> Math.max(0, 10 - dailyUsage);
            case PRO -> Math.max(0, 100 - dailyUsage);
            case ENTERPRISE -> 999999;
        };
    }
}