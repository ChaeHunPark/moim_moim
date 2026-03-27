package com.example.backend.dto;

import com.example.backend.entity.MeetingPost;
import com.example.backend.entity.Participation;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MeetingSummaryResponse {
    private Long id;
    private String title;
    private String categoryName;
    private LocalDateTime startDate;
    private int capacity;
    private int currentParticipants;

    @JsonProperty("isHost")
    private boolean isHost;

    // 💡 신청 상태 필드 추가 (null일 경우 방장이 만든 모임으로 해석 가능)
    private String status;

    // 1️⃣ 기존 방식: 내가 만든 모임(방장) 조회 시 사용
    public static MeetingSummaryResponse from(MeetingPost post, boolean isHost) {
        return MeetingSummaryResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .categoryName(post.getCategory().getName())
                .startDate(post.getStartDate())
                .capacity(post.getCapacity())
                .currentParticipants(post.getCurrentParticipants())
                .isHost(isHost)
                .status(null) // 방장은 본인 모임에 status가 필요 없음
                .build();
    }

    // 2️⃣ 새로운 방식: 내가 신청한 모임 조회 시 사용 (Participation 기반)
    public static MeetingSummaryResponse from(Participation participation) {
        MeetingPost post = participation.getMeetingPost();
        return MeetingSummaryResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .categoryName(post.getCategory().getName())
                .startDate(post.getStartDate())
                .capacity(post.getCapacity())
                .currentParticipants(post.getCurrentParticipants())
                .isHost(false) // 신청 내역이므로 무조건 방장이 아님
                .status(participation.getStatus().name()) // APPROVED, PENDING 등
                .build();
    }
}