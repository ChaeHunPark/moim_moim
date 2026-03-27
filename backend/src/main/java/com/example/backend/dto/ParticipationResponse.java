package com.example.backend.dto;

import com.example.backend.entity.Member;
import com.example.backend.entity.Participation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParticipationResponse {
    private Long participationId;
    private Long memberId;
    private String nickname;    // 💡 nickname으로 변경
    private String email;       // 💡 memberEmail 대신 단순하게 email
    private String status;
    private String joinReason;
    private String role;
    private LocalDateTime appliedAt;

    public static ParticipationResponse from(Participation participation) {
        Member m = participation.getMember(); // 반복 호출 방지
        if (m == null) return null; // 또는 기본값을 가진 DTO 반환
        return ParticipationResponse.builder()
                .participationId(participation.getId())
                .memberId(m.getId())
                .nickname(m.getNickname()) // 💡 getName() -> getNickname()
                .email(m.getEmail())
                .status(participation.getStatus().name())
                .joinReason(participation.getJoinReason())
                .appliedAt(participation.getCreatedAt())
                .role(participation.getRole() != null ? participation.getRole().name() : null)
                .build();
    }
}
