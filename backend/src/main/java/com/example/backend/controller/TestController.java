package com.example.backend.controller;

import com.example.backend.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final SseService sseService;

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

    @PostMapping("/send/{memberId}")
    public void testSend(@PathVariable("memberId") Long memberId) {
        // 1. memberId = 6
        // 2. eventName = "newNotification" (프론트 리스너와 동일하게!)
        // 3. data = "진짜 보내고 싶은 알림 내용"
        sseService.send(memberId, "newNotification", "드디어 알림 전송에 성공했습니다! 🎉");

        System.out.println("보내기 버튼 눌려쪙");
    }
}
