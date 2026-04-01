package com.example.backend.dto;

import com.example.backend.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String content;
    private String url;
    private boolean isRead;
    private String createdAt; // 프론트에서 읽기 편하게 String으로 변환

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .content(notification.getContent())
                .url(notification.getUrl())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt().toString()) // LocalDateTime -> String
                .build();
    }
}
