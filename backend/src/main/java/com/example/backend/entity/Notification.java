package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private Member receiver; // 알림을 받는 사용자 (Member 엔티티)

    @Column(nullable = false)
    private String content;  // 알림 내용 (예: "OO 모임에 승인되었습니다!")

    private String url;      // 클릭 시 이동할 프론트엔드 경로

    @Column(nullable = false)
    private boolean isRead;  // 읽음 여부

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public Notification(Member receiver, String content, String url) {
        this.receiver = receiver;
        this.content = content;
        this.url = url;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    // 알림 읽음 처리 로직
    public void markAsRead() {
        this.isRead = true;
    }
}
