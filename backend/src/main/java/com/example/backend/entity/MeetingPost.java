package com.example.backend.entity;

import com.example.backend.common.exception.CustomException;
import com.example.backend.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
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

    @Builder.Default
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

    @OneToMany(mappedBy = "meetingPost", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Participation> participations = new ArrayList<>();

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


    /**
     * 모임 정보 수정 (비즈니스 로직)
     * Dirty Checking에 의해 트랜잭션 종료 시 자동으로 DB에 반영됩니다.
     */
    public void update(String title, String description, int capacity, Category category,
                       LocalDateTime startDate, LocalDateTime endDate) {

        // [비즈니스 검증] 예: 현재 참여 인원보다 정원을 적게 수정할 수 없음
        if (capacity < this.currentParticipants) {
            throw new CustomException(ErrorCode.INVALID_CAPACITY);
            // ErrorCode에 "현재 참여 인원보다 적은 정원으로 수정할 수 없습니다" 추가 권장
        }

        this.title = title;
        this.description = description;
        this.capacity = capacity;
        this.category = category;
        this.startDate = startDate;
        this.endDate = endDate;
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
