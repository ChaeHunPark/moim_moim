package com.example.backend.controller;

import com.example.backend.dto.RegisterRequest;
import com.example.backend.dto.TokenResponseDto;
import com.example.backend.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf; // CSRF 추가
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    @WithMockUser // 가짜 사용자 생성
    @DisplayName("회원가입 API 테스트 - 201 Created 반환")
    void register_test() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@test.com")
                .password("password123")
                .nickname("테스터")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf()) // CSRF 토큰 시뮬레이션
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다!"));
    }

    @Test
    @WithMockUser
    @DisplayName("로그인 API 테스트 - 쿠키 설정 및 토큰 반환")
    void login_test() throws Exception {
        TokenResponseDto responseDto = TokenResponseDto.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .refreshTokenExpirationTime(3600000L)
                .build();

        given(authService.login(any(), any())).willReturn(responseDto);

        Map<String, String> loginRequest = Map.of(
                "email", "test@test.com",
                "password", "password123"
        );

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf()) // CSRF 토큰 시뮬레이션
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(jsonPath("$.accessToken").value("access-token"));
    }

    @Test
    @WithMockUser
    @DisplayName("로그아웃 API 테스트 - 쿠키 만료")
    void logout_test() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf()) // CSRF 토큰 시뮬레이션
                        .header("Authorization", "Bearer some-access-token"))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")));
    }
}