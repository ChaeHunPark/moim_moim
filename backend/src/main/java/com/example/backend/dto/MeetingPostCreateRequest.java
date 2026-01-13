package com.example.backend.dto;


import com.example.backend.entity.Category;
import com.example.backend.entity.MeetingPost;
import com.example.backend.entity.Member;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MeetingPostCreateRequest {
    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "설명은 필수입니다.")
    private String description;

    @Min(value = 2, message = "최소 2명 이상이어야 합니다.")
    private int capacity;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @NotNull(message = "카테고리는 필수입니다.")
    private Long categoryId;

    public MeetingPost toEntity(Member creator, Category category) {
        return MeetingPost.builder()
                .title(this.title)
                .description(this.description) // 이름 일치 완료
                .capacity(this.capacity)       // 이름 일치 완료
                .startDate(this.startDate)
                .endDate(this.endDate)
                .creator(creator)
                .category(category)
                .build();
    }
}