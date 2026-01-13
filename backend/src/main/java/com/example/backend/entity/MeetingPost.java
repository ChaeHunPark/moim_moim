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
public class MeetingPost extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private int capacity; // maxParticipants 대신 capacity로 통일

    @Column(nullable = false)
    private int currentParticipants = 1;

    @Column(nullable = false)
    private int viewCount = 0;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private Member creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Builder // 생성자의 파라미터명이 빌더 메서드명이 됩니다.
    public MeetingPost(String title, String description, int capacity,
                       LocalDateTime startDate, LocalDateTime endDate,
                       Member creator, Category category) {
        this.title = title;
        this.description = description;
        this.capacity = capacity;
        this.startDate = startDate;
        this.endDate = endDate;
        this.creator = creator;
        this.category = category;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    // 참여 인원 증가 (신규 신청 시 사용)
    public void addParticipant() {
        // 1. 엣지 케이스 검증: 현재 인원이 정원(capacity)과 같거나 큰지 확인
        if (this.currentParticipants >= this.capacity) {
            throw new IllegalStateException("이미 모집 정원(" + this.capacity + "명)이 가득 찼습니다.");
        }

        // 2. 상태 변경: 필드명 일관성 유지 (currentCount -> currentParticipants)
        this.currentParticipants++;
    }

    private void validateDateOrder(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException("시작 시간은 종료 시간보다 빨라야 합니다.");
        }
    }

    private void validateCapacity(int capacity) {
        if (capacity < 2) {
            throw new IllegalArgumentException("모임 인원은 최소 2명 이상이어야 합니다.");
        }
    }
}
