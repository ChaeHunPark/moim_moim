package com.example.backend.common.exception;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ErrorResponse {
    private final int status;    // HTTP 상태 코드 (예: 404)
    private final String code;   // 비즈니스 에러 코드 (예: MEETING_001)
    private final String message; // 사용자 메시지
}
