package com.example.backend.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Member
    MEMBER_NOT_FOUND(404, "MEMBER_001", "회원을 찾을 수 없습니다."),
    REGION_NOT_FOUND(404, "MEMBER_002", "존재하지 않는 지역입니다."),
    DUPLICATE_EMAIL(409, "MEMBER_002", "이미 사용 중인 이메일입니다."),

    // Auth
    INVALID_PASSWORD(401, "AUTH_001", "비밀번호가 일치하지 않습니다."),
    INVALID_TOKEN(401, "AUTH_002", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(401, "AUTH_003", "만료된 토큰입니다."),
    FORBIDDEN_ACCESS(403, "AUTH_004", "해당 리소스에 대한 접근 권한이 없습니다."), // 추가

    // Meeting
    MEETING_NOT_FOUND(404, "MEETING_001", "해당 모임을 찾을 수 없습니다."),
    CATEGORY_NOT_FOUND(404, "MEETING_002", "카테고리를 찾을 수 없습니다."),
    NOT_MEETING_CREATOR(403, "MEETING_003", "모임 수정/삭제 권한이 없습니다."),

    // Participation
    ALREADY_PARTICIPATED(400, "PART_001", "이미 신청했거나 참여 중인 모임입니다."),
    MEETING_FULL(400, "PART_002", "모임 정원이 가득 찼습니다.");

    private final int status;
    private final String code;
    private final String message;
}