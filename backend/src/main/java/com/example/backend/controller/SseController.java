package com.example.backend.controller;

import com.example.backend.common.exception.CustomException;
import com.example.backend.common.exception.ErrorCode;
import com.example.backend.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SseController {

    private final SseService sseService;

    /**
     * SSE 실시간 알림 구독
     * @param authentication SecurityContext에서 가져온 인증 객체
     * @return SseEmitter (실시간 데이터 스트림)
     */
    @GetMapping(value = "/api/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(Authentication authentication) {

        // 1. 인증 체크 (ErrorCode.INVALID_TOKEN 활용 가능)
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("인증되지 않은 사용자의 SSE 구독 시도");
            // 프로젝트의 예외 처리 방식에 맞춰 CustomException
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        try {
            // 2. Authentication.getName()을 통해 Member ID 추출
            // (시큐리티 설정 시 principal에 memberId를 넣었다고 가정)
            Long memberId = Long.valueOf(authentication.getName());

            log.info("SSE 연결 시작: memberId = {}", memberId);

            // 3. SseService를 통해 Emitter 생성 및 반환
            return sseService.subscribe(memberId);

        } catch (NumberFormatException e) {
            log.error("Authentication Name이 유효한 ID 형식이 아닙니다: {}", authentication.getName());
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }
}
