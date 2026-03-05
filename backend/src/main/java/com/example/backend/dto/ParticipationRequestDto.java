package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequestDto {

    @NotNull(message = "모임 ID는 필수입니다.")
    private Long meetingPostId;

    @NotBlank(message = "참여 이유를 입력해주세요.")
    @Size(max = 500, message = "참여 이유는 500자 이내로 작성해주세요.")
    private String joinReason;
}