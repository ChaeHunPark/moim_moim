package com.example.backend.service;

import com.example.backend.common.exception.CustomException;
import com.example.backend.common.exception.ErrorCode;
import com.example.backend.dto.NotificationResponse;
import com.example.backend.entity.Member;
import com.example.backend.entity.Notification;
import com.example.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * 1. 알림 생성 및 저장
     *
     * @param receiver 알림을 받을 회원
     * @param content  알림 메시지 내용
     * @param url      클릭 시 이동할 경로
     */
    @Transactional
    public void createNotification(Member receiver, String content, String url) {
        Notification notification = Notification.builder()
                .receiver(receiver)
                .content(content)
                .url(url)
                .build();
        notificationRepository.save(notification);

        // 💡 SSE 구현 시 여기에 '실시간 전송' 로직이 추가됩니다.
    }

    /**
     * 2. 내 알림 목록 조회 (최신순)
     *
     * @param memberId 로그인한 회원의 ID
     */
    public List<NotificationResponse> getMyNotifications(Long memberId) {
        return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    /**
     * 3. 알림 읽음 처리 (보안 로직 포함)
     *
     * @param notificationId 읽음 처리할 알림 ID
     * @param memberId       현재 로그인한 회원의 ID (권한 확인용)
     */
    @Transactional
    public void markAsRead(Long notificationId, Long memberId) {
        Notification notification = getNotificationWithCheck(notificationId, memberId);
        notification.markAsRead(); // Entity 내부의 isRead = true;
    }

    /**
     * 4. 알림 삭제 (보안 로직 포함)
     *
     * @param notificationId 삭제할 알림 ID
     * @param memberId       현재 로그인한 회원의 ID (권한 확인용)
     */
    @Transactional
    public void deleteNotification(Long notificationId, Long memberId) {
        Notification notification = getNotificationWithCheck(notificationId, memberId);
        notificationRepository.delete(notification);
    }

    /**
     * [공통 로직] 알림 존재 여부 및 소유권 확인
     */
    private Notification getNotificationWithCheck(Long notificationId, Long memberId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));

        // 해당 알림의 수신자가 로그인한 유저와 일치하는지 확인
        if (!notification.getReceiver().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.NOT_AUTHORIZED_NOTIFICATION);
        }

        return notification;
    }
}
