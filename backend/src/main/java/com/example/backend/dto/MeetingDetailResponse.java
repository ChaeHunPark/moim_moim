package com.example.backend.dto;

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
    private String creatorEmail;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
}
