package com.example.backend.controller;

import com.example.backend.dto.ParticipationRequestDto;
import com.example.backend.service.ParticipationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

@WebMvcTest(ParticipationController.class) // 특정 컨트롤러만 로드하여 가볍게 테스트
@AutoConfigureMockMvc
class ParticipationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ParticipationService participationService; // 서비스는 가짜로 대체

    @Autowired
    private ObjectMapper objectMapper; // DTO를 JSON으로 변환용

    @Test
    @DisplayName("참여 신청 API 성공 - 인증된 사용자")
    @WithMockUser(username = "1") // Principal에 "1"(String)이 들어가도록 설정
    void applySuccess() throws Exception {
        // given
        ParticipationRequestDto requestDto = new ParticipationRequestDto(100L, "참여 희망합니다!");
        given(participationService.applyForMeeting(any(), anyLong())).willReturn(1L);

        // when & then
        mockMvc.perform(post("/api/participation/apply")
                        .with(csrf()) // 스프링 시큐리티 CSRF 대응 (설정에 따라 필요)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated()) // 201 응답 확인
                .andExpect(content().string("1")); // 생성된 ID 확인

        verify(participationService).applyForMeeting(any(), eq(1L));
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 참여 신청 불가")
    void applyUnauthorized() throws Exception {
        ParticipationRequestDto requestDto = new ParticipationRequestDto(100L, "안녕");

        mockMvc.perform(post("/api/participation/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden()); // 403 혹은 401 확인
    }

    @Test
    @DisplayName("참여 신청 상태 변경 성공 - 방장 승인")
    @WithMockUser(username = "1")
    void updateStatus_Success() throws Exception {
        // given
        Long participationId = 500L;
        String status = "ACCEPTED";
        given(participationService.updateParticipationStatus(eq(participationId), eq(status), eq(1L)))
                .willReturn(participationId);

        // when & then
        mockMvc.perform(patch("/api/participation/{participationId}/status", participationId)
                        .with(csrf())
                        .param("status", status)) // @RequestParam 대응
                .andExpect(status().isOk())
                .andExpect(content().string("500"));

        verify(participationService).updateParticipationStatus(eq(500L), eq("ACCEPTED"), eq(1L));
    }
}