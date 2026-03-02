package com.example.backend.controller;


import com.example.backend.dto.MeetingDetailResponse;
import com.example.backend.dto.MeetingPostCreateRequest;
import com.example.backend.service.MeetingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;



import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MeetingPostController.class) // 테스트할 컨트롤러 지정
class MeetingPostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean // 서비스 계층 모킹
    private MeetingService meetingService;

    @Test
    @WithMockUser // 스프링 시큐리티 권한 처리 (로그인 상태 가정)
    @DisplayName("모임 생성 API 호출 성공 - 201 Created를 반환한다")
    void createMeeting_api_success() throws Exception {
        // [Given]
        MeetingPostCreateRequest request = MeetingPostCreateRequest.builder()
                .title("풋살 하실 분")
                .description("강남역 인근입니다")
                .capacity(10)
                .categoryId(1L)
                .build();

        given(meetingService.createMeeting(any(), any())).willReturn(100L);

        // [When & Then]
        mockMvc.perform(post("/api/meetings")
                        .with(csrf()) // 시큐리티 사용 시 CSRF 토큰 필요
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()) // 201 응답 기대
                .andExpect(jsonPath("$.id").value(100L)); // 반환 데이터 검증
    }

    @Test
    @WithMockUser
    @DisplayName("모임 상세 조회 API 호출 성공 - 200 OK와 데이터를 반환한다")
    void getMeetingDetail_api_success() throws Exception {
        // [Given]
        Long meetingId = 100L;
        MeetingDetailResponse response = MeetingDetailResponse.builder()
                .id(meetingId)
                .title("러닝 스터디")
                .description("3km 뜁니다")
                .capacity(5)
                .currentParticipants(1)
                .categoryName("운동")
                .creatorEmail("user@test.com")
                .build();

        given(meetingService.getMeetingDetail(meetingId)).willReturn(response);

        // [When & Then]
        mockMvc.perform(get("/api/meetings/{id}", meetingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("러닝 스터디"))
                .andExpect(jsonPath("$.categoryName").value("운동"))
                .andExpect(jsonPath("$.currentParticipants").value(1));
    }
}