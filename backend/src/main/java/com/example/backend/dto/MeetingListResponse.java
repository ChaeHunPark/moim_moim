package com.example.backend.dto;

import com.example.backend.entity.MeetingPost;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class MeetingListResponse {
    private Long id;
    private String title;
    private String categoryName;
    private String creatorEmail;
    private int capacity;
    private int currentParticipants;
    private int viewCount;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime startDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime endDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;
    private boolean isClosed; // 모집 종료 여부

    // Entity -> DTO 변환 메서드
    public static MeetingListResponse from(MeetingPost post) {
        return MeetingListResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                // Null 발생 가능성이 있는 연관관계는 안전하게 처리
                .categoryName(post.getCategory() != null ? post.getCategory().getName() : "미지정")
                .creatorEmail(post.getCreator() != null ? post.getCreator().getEmail() : "알 수 없음")
                .capacity(post.getCapacity())
                .currentParticipants(post.getCurrentParticipants())
                .viewCount(post.getViewCount())
                .startDate(post.getStartDate())
                .endDate(post.getEndDate())
                .createdAt(post.getCreatedAt())
                .isClosed(post.getCurrentParticipants() >= post.getCapacity())
                .build();
    }
}
