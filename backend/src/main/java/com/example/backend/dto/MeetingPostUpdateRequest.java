package com.example.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MeetingPostUpdateRequest {

    @NotBlank(message = "모임 제목은 필수입니다.")
    private String title;

    @NotBlank(message = "모임 설명은 필수입니다.")
    private String description;

    @Min(value = 2, message = "모임 정원은 최소 2명 이상이어야 합니다.")
    private int capacity;

    @NotNull(message = "카테고리 선택은 필수입니다.")
    private Long categoryId;

    @NotNull(message = "모임 시작 시간은 필수입니다.")
    private LocalDateTime startDate;

    @NotNull(message = "모임 종료 시간은 필수입니다.")
    private LocalDateTime endDate;
}
