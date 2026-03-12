package com.example.backend.controller;


import com.example.backend.dto.MeetingDetailResponse;
import com.example.backend.dto.MeetingListResponse;
import com.example.backend.dto.MeetingPostCreateRequest;
import com.example.backend.entity.MeetingPost;
import com.example.backend.service.MeetingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MeetingPostController.class) // 테스트할 컨트롤러 지정
@AutoConfigureMockMvc(addFilters = false) // 시큐리티 필터 제외
class MeetingPostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean // 서비스 계층 모킹
    private MeetingService meetingService;

    @Test
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

    @Test
    @DisplayName("참여 신청 시 현재 인원이 1 증가한다")
    void addParticipant_Success() {
        // given
        MeetingPost post = MeetingPost.builder()
                .capacity(2)
                .build();

        // when
        post.addParticipant();

        // then
        assertThat(post.getCurrentParticipants()).isEqualTo(2);
    }

    @Test
    @DisplayName("정원이 초과된 상태에서 참여 신청 시 예외가 발생한다")
    void addParticipant_Fail_Full() {
        // given
        MeetingPost post = MeetingPost.builder()
                .capacity(2)
                .build();
        post.addParticipant(); // 현재 2명 (풀방)

        // when & then
        assertThatThrownBy(post::addParticipant)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 모집 정원");
    }

    @Test
    @DisplayName("정렬 조건 없이 목록 조회 시 기본값(latest)으로 서비스를 호출한다")
    void getAllMeetings_Default() throws Exception {
        // given
        given(meetingService.getAllMeetings("latest", null))
                .willReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/meetings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        verify(meetingService).getAllMeetings("latest", null);
    }

    @Test
    @DisplayName("특정 정렬 조건과 카테고리로 목록을 조회한다")
    void getAllMeetings_WithParams() throws Exception {
        // given
        Long categoryId = 1L;
        String sortBy = "popular";

        MeetingListResponse response = MeetingListResponse.builder()
                .id(1L)
                .title("인기 있는 모임")
                .categoryName("개발")
                .viewCount(100)
                .build();

        given(meetingService.getAllMeetings(sortBy, categoryId))
                .willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/meetings")
                        .param("sortBy", sortBy)
                        .param("categoryId", categoryId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("인기 있는 모임"))
                .andExpect(jsonPath("$[0].viewCount").value(100))
                .andDo(print());

        verify(meetingService).getAllMeetings(sortBy, categoryId);
    }

    @Test
    @DisplayName("잔여석 순(urgent) 정렬 파라미터가 서비스로 잘 전달된다")
    void getAllMeetings_UrgentParam() throws Exception {
        // given
        given(meetingService.getAllMeetings("urgent", null))
                .willReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/meetings")
                        .param("sortBy", "urgent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(meetingService).getAllMeetings("urgent", null);
    }
}