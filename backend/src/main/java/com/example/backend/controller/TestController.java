package com.example.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal String email) {
        // 인증 필터가 잘 작동하면 @AuthenticationPrincipal에 로그인한 이메일이 들어옵니다.
        return ResponseEntity.ok(Map.of(
                "message", "인가 성공!",
                "loginUser", email
        ));
    }

    // 1. 모든 로그인한 유저(USER, ADMIN)가 접근 가능
    @GetMapping("/user")
    public ResponseEntity<String> userAccess(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok("일반 유저 접속 성공: " + email);
    }

    // 2. 관리자(ADMIN)만 접근 가능
    @GetMapping("/admin")
    public ResponseEntity<String> adminAccess(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok("관리자 접속 성공: " + email);
    }
}
