package com.converter.document_converter.service;

import com.converter.document_converter.entity.User;
import com.converter.document_converter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        // 이메일 중복 체크
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);

        // 사용자 생성
        User user = User.builder()
                .email(email)
                .password(encodedPassword)
                .name(name)
                .role(User.UserRole.FREE)
                .apiKey(UUID.randomUUID().toString())
                .dailyUsage(0)
                .monthlyUsage(0)
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
    // 사용량 증가
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



}