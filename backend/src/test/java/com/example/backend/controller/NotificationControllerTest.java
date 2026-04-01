package com.example.backend.controller;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

import java.time.LocalDateTime;
import java.util.List;

import com.example.backend.dto.NotificationResponse;
import com.example.backend.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    @WithMockUser(username = "1") // Authentication.getName()이 "1"을 반환하도록 설정
    @DisplayName("알림 목록 조회 API 성공")
    void getNotifications_Success() throws Exception {
        // given
        NotificationResponse res1 = new NotificationResponse(101L, "알림1", "/url1", false, LocalDateTime.now().toString());
        NotificationResponse res2 = new NotificationResponse(102L, "알림2", "/url2", true, LocalDateTime.now().toString());

        given(notificationService.getMyNotifications(1L)).willReturn(List.of(res1, res2));

        // when & then
        mockMvc.perform(get("/api/notifications")
                        .with(csrf())) // Security 설정에 따라 필요할 수 있음
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].content").value("알림1"))
                .andExpect(jsonPath("$[1].id").value(102L));
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("알림 읽음 처리 PATCH API 성공")
    void readNotification_Success() throws Exception {
        // given
        Long notificationId = 100L;
        // 서비스 로직은 void이므로 별도 return 정의 불필요 (기본적으로 성공 가정)

        // when & then
        mockMvc.perform(patch("/api/notifications/{id}/read", notificationId)
                        .with(csrf()))
                .andExpect(status().isOk());

        // 서비스가 올바른 파라미터로 호출되었는지 검증
        verify(notificationService).markAsRead(notificationId, 1L);
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("알림 삭제 DELETE API 성공")
    void deleteNotification_Success() throws Exception {
        // given
        Long notificationId = 100L;

        // when & then
        mockMvc.perform(delete("/api/notifications/{id}", notificationId)
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(notificationService).deleteNotification(notificationId, 1L);
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 조회 시 401 또는 403 발생")
    void getNotifications_Fail_UnAuthenticated() throws Exception {
        // @WithMockUser 없음
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().is4xxClientError());
    }
}