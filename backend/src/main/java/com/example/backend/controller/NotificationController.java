package com.example.backend.controller;

import com.example.backend.dto.NotificationResponse;
import com.example.backend.entity.Notification;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.attribute.UserPrincipal;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 1. 알림 목록 조회 (GET)
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            Authentication authentication) {

        // 인증 정보에서 memberId 추출
        Long memberId = Long.valueOf(authentication.getName());

        // 서비스 호출: DTO 리스트 반환
        List<NotificationResponse> response = notificationService.getMyNotifications(memberId);

        return ResponseEntity.ok(response);
    }

    /**
     * 2. 알림 읽음 처리 (PATCH)
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> readNotification(
            @PathVariable("id") Long id,
            Authentication authentication) {

        Long memberId = Long.valueOf(authentication.getName());

        // 서비스 호출: 존재 여부 및 소유권 확인 후 읽음 처리
        notificationService.markAsRead(id, memberId);

        return ResponseEntity.ok().build();
    }

    /**
     * 3. 알림 삭제 (DELETE)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable("id") Long id,
            Authentication authentication) {

        Long memberId = Long.valueOf(authentication.getName());

        // 서비스 호출: 존재 여부 및 소유권 확인 후 삭제
        notificationService.deleteNotification(id, memberId);

        return ResponseEntity.ok().build();
    }
}
