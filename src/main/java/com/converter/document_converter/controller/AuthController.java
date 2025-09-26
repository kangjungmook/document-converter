package com.converter.document_converter.controller;

import com.converter.document_converter.entity.User;
import com.converter.document_converter.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    // 회원가입 페이지
    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    // 로그인 페이지
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // 회원가입 처리
    @PostMapping("/api/signup")
    @ResponseBody
    public Map<String, Object> signup(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String name
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = userService.register(email, password, name);

            response.put("status", "SUCCESS");
            response.put("message", "회원가입이 완료되었습니다!");
            response.put("user", Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "name", user.getName(),
                    "role", user.getRole()
            ));
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
        }

        return response;
    }

    // 로그인 처리
    @PostMapping("/api/login")
    @ResponseBody
    public Map<String, Object> login(
            @RequestParam String email,
            @RequestParam String password
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = userService.login(email, password);

            response.put("status", "SUCCESS");
            response.put("message", "로그인 성공!");
            response.put("user", Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "name", user.getName(),
                    "role", user.getRole(),
                    "apiKey", user.getApiKey()
            ));
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
        }

        return response;
    }
}