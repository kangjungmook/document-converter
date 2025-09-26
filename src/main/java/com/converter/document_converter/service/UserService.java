package com.converter.document_converter.service;

import com.converter.document_converter.entity.User;
import com.converter.document_converter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    @Transactional
    public User register(String email, String password, String name) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(password);

        User user = User.builder()
                .email(email)
                .password(encodedPassword)
                .name(name)
                .role(User.UserRole.FREE)
                .apiKey(UUID.randomUUID().toString())
                .dailyUsage(0)
                .monthlyUsage(0)
                .pdfMergeUsage(0)
                .pdfSplitUsage(0)
                .imageResizeUsage(0)
                .imageCompressUsage(0)
                .isActive(true)
                .build();

        return userRepository.save(user);
    }

    // 로그인
    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return user;
    }

    // 이메일로 사용자 찾기
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // API 키로 사용자 찾기
    public Optional<User> findByApiKey(String apiKey) {
        return userRepository.findByApiKey(apiKey);
    }

    // ===== 기능별 사용량 체크 및 증가 =====
    @Transactional
    public void incrementUsage(Long userId, User.FeatureType feature) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 전체 사용 가능 여부 체크
        if (!user.canUseService()) {
            String limit = user.getRole() == User.UserRole.FREE ? "10회" : "100회";
            throw new RuntimeException("일일 사용 한도 초과 (" + user.getRole() + ": " + limit + "/일)");
        }

        // 기능별 사용 가능 여부 체크
        if (!user.canUseFeature(feature)) {
            String featureName = getFeatureName(feature);
            String limit = user.getRole() == User.UserRole.FREE ? "10회" : "100회";
            throw new RuntimeException(featureName + " 기능 일일 한도 초과 (" + limit + "/일)");
        }

        // 사용량 증가
        user.incrementFeatureUsage(feature);
        userRepository.save(user);
    }

    // ===== 기존 방식 (하위 호환성) =====
    @Transactional
    public void incrementUsage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!user.canUseService()) {
            throw new RuntimeException("일일 사용 한도 초과 (FREE: 10회/일)");
        }

        user.incrementUsage();
        userRepository.save(user);
    }

    // 파일 크기 체크
    public void checkFileSize(Long userId, long fileSize) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        long limit = user.getFileSizeLimit();
        if (fileSize > limit) {
            String limitMB = String.format("%.0f", limit / 1024.0 / 1024.0);
            throw new RuntimeException("파일 크기 제한 초과 (최대: " + limitMB + "MB)");
        }
    }

    // ===== 매일 자정 사용량 초기화 스케줄러 =====
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정
    @Transactional
    public void resetDailyUsage() {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            user.resetDailyUsage();
        }

        userRepository.saveAll(users);
        System.out.println("✅ 일일 사용량 초기화 완료: " + LocalDateTime.now());
    }

    // ===== Helper 메서드 =====
    private String getFeatureName(User.FeatureType feature) {
        return switch (feature) {
            case PDF_MERGE -> "PDF 병합";
            case PDF_SPLIT -> "PDF 분할";
            case IMAGE_RESIZE -> "이미지 리사이즈";
            case IMAGE_COMPRESS -> "이미지 압축";
        };
    }

    // ===== 사용량 정보 조회 =====
    public UsageInfo getUserUsage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return UsageInfo.builder()
                .dailyUsage(user.getDailyUsage())
                .remainingUsage(user.getRemainingUsage())
                .pdfMergeUsage(user.getPdfMergeUsage() != null ? user.getPdfMergeUsage() : 0)
                .pdfSplitUsage(user.getPdfSplitUsage() != null ? user.getPdfSplitUsage() : 0)
                .imageResizeUsage(user.getImageResizeUsage() != null ? user.getImageResizeUsage() : 0)
                .imageCompressUsage(user.getImageCompressUsage() != null ? user.getImageCompressUsage() : 0)
                .monthlyUsage(user.getMonthlyUsage())
                .role(user.getRole().toString())
                .build();
    }

    @lombok.Data
    @lombok.Builder
    public static class UsageInfo {
        private Integer dailyUsage;
        private Integer remainingUsage;
        private Integer pdfMergeUsage;
        private Integer pdfSplitUsage;
        private Integer imageResizeUsage;
        private Integer imageCompressUsage;
        private Integer monthlyUsage;
        private String role;
    }
}