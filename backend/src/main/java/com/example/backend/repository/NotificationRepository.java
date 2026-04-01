package com.example.backend.repository;

import com.example.backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 특정 회원의 알림 목록을 최신순으로 조회 (N+1 방지를 위해 필요시 fetch join 고려)
    List<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);

    // 읽지 않은 알림이 있는지 확인 (프론트 헤더의 '!' 표시 여부 결정용)
    boolean existsByReceiverIdAndIsReadFalse(Long receiverId);

    // 특정 회원의 읽지 않은 알림 개수
    long countByReceiverIdAndIsReadFalse(Long receiverId);
}
