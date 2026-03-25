package com.example.backend.controller;


import com.example.backend.common.exception.CustomException;
import com.example.backend.common.exception.ErrorCode;
import com.example.backend.dto.MeetingDetailResponse;
import com.example.backend.dto.MeetingPostCreateRequest;
import com.example.backend.dto.MeetingPostUpdateRequest;
import com.example.backend.service.MeetingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MeetingPostController.class)
@AutoConfigureMockMvc(addFilters = false) // 시큐리티 필터 제외 (테스트 집중)
class MeetingPostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean // Spring Boot 버전에 따라 @MockitoBean 또는 @MockBean 사용
    private MeetingService meetingService;

    // --- 1. 모임 생성 테스트 ---
    @Test
    @DisplayName("모임 생성 API 호출 성공 - 201 Created를 반환한다")
    void createMeeting_api_success() throws Exception {
        MeetingPostCreateRequest request = MeetingPostCreateRequest.builder()
                .title("테스트 모임").description("설명").capacity(5).categoryId(1L).build();

        given(meetingService.createMeeting(any(), any())).willReturn(100L);

        mockMvc.perform(post("/api/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L));
    }

    // --- 2. 모임 상세 조회 테스트 ---
    @Test
    @DisplayName("모임 상세 조회 API 호출 성공 - 200 OK와 데이터를 반환한다")
    void getMeetingDetail_api_success() throws Exception {
        // given
        Long meetingId = 100L;
        MeetingDetailResponse response = MeetingDetailResponse.builder()
                .id(meetingId)
                .title("러닝 스터디")
                .categoryName("운동")
                .currentParticipants(1)
                .isHost(true) // 💡 새롭게 추가된 필드 설정
                .build();

        // 💡 서비스 호출 시 두 번째 인자로 어떤 String이 와도 response를 반환하도록 설정
        given(meetingService.getMeetingDetail(eq(meetingId), any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/meetings/{id}", meetingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("러닝 스터디"))
                .andExpect(jsonPath("$.id").value(meetingId))
                .andExpect(jsonPath("$.host").value(true)); // 💡 isHost 필드 확인 (Boolean은 $.host로 매핑될 수 있음)
    }

    // --- 3. 모임 수정 테스트 (Update) ---
    @Test
    @DisplayName("모임 수정 API 호출 성공 - 200 OK를 반환한다")
    void updateMeeting_api_success() throws Exception {
        Long meetingId = 100L;
        MeetingPostUpdateRequest request = MeetingPostUpdateRequest.builder()
                .title("제목 수정").description("내용 수정").capacity(10).categoryId(1L)
                .startDate(LocalDateTime.now().plusDays(1)).endDate(LocalDateTime.now().plusDays(2))
                .build();

        willDoNothing().given(meetingService).updateMeeting(eq(meetingId), any(), any());

        mockMvc.perform(put("/api/meetings/{id}", meetingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("모임 수정 실패 - 작성자가 아닐 때 403 Forbidden과 ErrorCode를 반환한다")
    void updateMeeting_api_fail_forbidden() throws Exception {
        Long meetingId = 100L;
        MeetingPostUpdateRequest request = MeetingPostUpdateRequest.builder()
                .title("제목")
                .description("내용")
                .capacity(5)
                .categoryId(1L)
                .startDate(LocalDateTime.now().plusDays(1)) // 추가
                .endDate(LocalDateTime.now().plusDays(2))   // 추가
                .build();

        willThrow(new CustomException(ErrorCode.NOT_MEETING_CREATOR))
                .given(meetingService).updateMeeting(eq(meetingId), any(), any());

        mockMvc.perform(put("/api/meetings/{id}", meetingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("MEETING_003"));
    }

    // --- 4. 모임 삭제 테스트 (Delete) ---
    @Test
    @DisplayName("모임 삭제 API 호출 성공 - 204 No Content를 반환한다")
    void deleteMeeting_api_success() throws Exception {
        Long meetingId = 100L;
        willDoNothing().given(meetingService).deleteMeeting(eq(meetingId), any());

        mockMvc.perform(delete("/api/meetings/{id}", meetingId))
                .andExpect(status().isNoContent());
    }

    // --- 5. 목록 조회 및 필터링 테스트 ---
    @Test
    @DisplayName("정렬 조건 없이 목록 조회 시 기본값(latest)으로 서비스를 호출한다")
    void getAllMeetings_Default() throws Exception {
        given(meetingService.getAllMeetings("latest", null)).willReturn(List.of());

        mockMvc.perform(get("/api/meetings"))
                .andExpect(status().isOk());

        verify(meetingService).getAllMeetings("latest", null);
    }

    @Test
    @DisplayName("특정 정렬 조건(popular) 파라미터가 서비스로 잘 전달된다")
    void getAllMeetings_WithParams() throws Exception {
        given(meetingService.getAllMeetings("popular", 1L)).willReturn(List.of());

        mockMvc.perform(get("/api/meetings")
                        .param("sortBy", "popular")
                        .param("categoryId", "1"))
                .andExpect(status().isOk());

        verify(meetingService).getAllMeetings("popular", 1L);
    }

    @Test
    @DisplayName("유효성 검사 실패 - 제목 공백 시 400 Bad Request를 반환한다")
    void updateMeeting_api_fail_validation() throws Exception {
        Long meetingId = 100L;
        MeetingPostUpdateRequest invalidRequest = MeetingPostUpdateRequest.builder()
                .title("").description("내용").build(); // @NotBlank 위반

        mockMvc.perform(put("/api/meetings/{id}", meetingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_001"));
    }
}