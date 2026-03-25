package com.example.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor // Builder와 함께 사용하기 위한 생성자
public class MeetingDetailResponse {
    private Long id;
    private String title;
    private String description;
    private int capacity;
    private int currentParticipants;
    private int viewCount;
    private String categoryName;
    private Long categoryId; // 수정시 프론트 Select 박스 초기값용
    private String creatorEmail;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    @JsonProperty("isHost") // JSON 키값을 무조건 "isHost"로 고정
    private boolean isHost;
}
