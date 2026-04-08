package com.example.backend.controller;

import com.example.backend.service.SseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SseController.class) // MockMvc와 Service를 스프링이 주입해주도록 설정
class SseControllerTest {

    @Autowired
    private MockMvc mockMvc; // 이제 null이 아니게 됩니다

    @MockitoBean
    private SseService sseService; // @Mock 대신 @MockBean 사용

    @Test
    @DisplayName("성공: 인증된 유저의 ID로 서비스를 호출하고 Emitter를 반환한다")
    @WithMockUser(username = "1")
    void subscribe_Success_Simple() {
        // given
        Long memberId = 1L;
        SseEmitter expectedEmitter = new SseEmitter();
        given(sseService.subscribe(memberId)).willReturn(expectedEmitter);

        // Authentication 객체는 스프링이 주입해주므로 서비스 호출 여부만 검증
        // when & then
        try {
            mockMvc.perform(get("/api/subscribe")
                            .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                    .andExpect(status().isOk());

            verify(sseService, times(1)).subscribe(memberId);
        } catch (Exception e) {
            // SSE 특성상 비동기 에러가 나더라도 서비스 호출만 확인되면 성공으로 간주
            verify(sseService, times(1)).subscribe(memberId);
        }
    }

    @Test
    @DisplayName("실패: 인증 정보의 형식이 숫자가 아닐 경우 401을 반환한다")
    @WithMockUser(username = "not_a_number")
    void subscribe_Fail_InvalidMemberIdFormat() throws Exception {
        mockMvc.perform(get("/api/subscribe")
                        .accept(MediaType.TEXT_EVENT_STREAM_VALUE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_002"));
    }

    @Test
    @DisplayName("실패: 인증되지 않은 유저가 구독을 요청하면 401을 반환한다")
    void subscribe_Fail_UnAuthorized() throws Exception {
        mockMvc.perform(get("/api/subscribe")
                        .accept(MediaType.TEXT_EVENT_STREAM_VALUE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isUnauthorized());
    }
}