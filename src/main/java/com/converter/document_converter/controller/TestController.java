package com.converter.document_converter.controller;

import com.converter.document_converter.entity.User;
import com.converter.document_converter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final UserRepository userRepository;

    // 데이터베이스 연결 테스트
    @GetMapping("/db")
    public Map<String, Object> testDatabase() {
        Map<String, Object> response = new HashMap<>();

        try {
            long userCount = userRepository.count();
            response.put("status", "SUCCESS");
            response.put("message", "데이터베이스 연결 성공!");
            response.put("userCount", userCount);
            response.put("timestamp", LocalDateTime.now());
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
        }

        return response;
    }

    // 테스트 사용자 생성
    @PostMapping("/create-user")
    public Map<String, Object> createTestUser() {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = User.builder()
                    .email("test" + System.currentTimeMillis() + "@convertio.com")
                    .password("$2a$10$dummyHashedPassword")
                    .name("테스트 사용자")
                    .role(User.UserRole.FREE)
                    .apiKey(UUID.randomUUID().toString())
                    .dailyUsage(0)
                    .monthlyUsage(0)
                    .isActive(true)
                    .build();

            User savedUser = userRepository.save(user);

            response.put("status", "SUCCESS");
            response.put("message", "사용자 생성 완료!");
            response.put("user", Map.of(
                    "id", savedUser.getId(),
                    "email", savedUser.getEmail(),
                    "name", savedUser.getName(),
                    "role", savedUser.getRole(),
                    "apiKey", savedUser.getApiKey()
            ));
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
        }

        return response;
    }

    // 모든 사용자 조회
    @GetMapping("/users")
    public Map<String, Object> getAllUsers() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<User> users = userRepository.findAll();

            response.put("status", "SUCCESS");
            response.put("count", users.size());
            response.put("users", users);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
        }

        return response;
    }
}