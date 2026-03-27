package com.example.backend.controller;

import com.example.backend.dto.MeetingSummaryResponse;
import com.example.backend.dto.ParticipationResponse;
import com.example.backend.service.MeetingService;
import com.example.backend.service.ParticipationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(MyPageController.class)
@AutoConfigureMockMvc
class MyPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MeetingService meetingService;

    @MockitoBean
    private ParticipationService participationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("내가 만든 모임 목록 조회 성공 - 방장 모드")
    @WithMockUser(username = "1")
    void getMyCreatedMeetings_Success() throws Exception {
        // given
        MeetingSummaryResponse response = MeetingSummaryResponse.builder()
                .title("내가 만든 모임")
                .categoryName("스터디")
                .isHost(true)
                .build();

        given(meetingService.getMyCreatedMeetings(1L)).willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/mypage/meetings/created"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].title").value("내가 만든 모임"))
                .andExpect(jsonPath("$[0].isHost").value(true));
    }

    @Test
    @DisplayName("특정 모임의 신청자 명단 조회 성공 - 방장 권한")
    @WithMockUser(username = "1")
    void getParticipants_Success() throws Exception {
        // given
        Long postId = 100L;
        ParticipationResponse participation = ParticipationResponse.builder()
                .participationId(500L)
                .nickname("신청자1")
                .joinReason("함께하고 싶어요")
                .status("APPLIED")
                .build();

        given(participationService.getParticipants(eq(postId), eq(1L)))
                .willReturn(List.of(participation));

        // when & then
        mockMvc.perform(get("/api/mypage/meeting/{postId}/participants", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].nickname").value("신청자1"))
                .andExpect(jsonPath("$[0].joinReason").value("함께하고 싶어요"));
    }

    @Test
    @DisplayName("내가 신청한 모임 목록 조회 성공 - 참여자 모드")
    @WithMockUser(username = "1")
    void getMyAppliedMeetings_Success() throws Exception {
        // given
        MeetingSummaryResponse response = MeetingSummaryResponse.builder()
                .title("내가 신청한 모임")
                .isHost(false)
                .build();

        given(meetingService.getMyAppliedMeetings(1L)).willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/mypage/meetings/applied"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].title").value("내가 신청한 모임"))
                .andExpect(jsonPath("$[0].isHost").value(false));
    }
}