package com.example.backend.common.security;

import com.example.backend.common.exception.CustomJwtException;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/*
* API 요청은 이필터를 반드시 통과
* */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper(); // 재사용 가능하게 필드로 둠

    private final StringRedisTemplate redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Request Header에서 토큰 추출
        String token = resolveToken(request);

        // 2. 토큰이 존재할 때만 검증 로직 실행
        if (StringUtils.hasText(token)) {
            try {
                // jwtTokenProvider.validateToken 내에서 만료/변조 시 CustomJwtException 발생
                if (jwtTokenProvider.validateToken(token)) {

                    // 3. Redis 블랙리스트(로그아웃 여부) 확인
                    String isLogout = redisTemplate.opsForValue().get("BL:" + token);

                    if (ObjectUtils.isEmpty(isLogout)) {
                        // 4. 정상 토큰일 경우 인증 객체 설정
                        Authentication auth = jwtTokenProvider.getAuthentication(token);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        log.debug("인증 완료 - Principal: {}", auth.getPrincipal());
                    } else {
                        // 로그아웃된 토큰인 경우
                        log.warn("로그아웃된 토큰으로 접근 시도: {}", token);
                        sendErrorResponse(response, "LOGOUT_TOKEN", "이미 로그아웃된 토큰입니다.");
                        return;
                    }
                }
            } catch (CustomJwtException e) {
                // 💡 [핵심] 커스텀 예외를 잡아 프론트엔드와 약속한 에러 코드를 응답
                log.warn("JWT 인증 실패 - 코드: {}, 사유: {}", e.getErrorCode(), e.getMessage());
                sendErrorResponse(response, e.getErrorCode(), e.getMessage());
                return; // 필터 중단
            } catch (Exception e) {
                // 그 외 알 수 없는 인증 에러
                log.error("인증 처리 중 서버 에러 발생", e);
                sendErrorResponse(response, "AUTH_ERROR", "인증 처리 중 오류가 발생했습니다.");
                return;
            }
        }

        // 5. 토큰이 없거나 인증이 완료된 경우 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    /**
     * 에러 응답을 보내는 메서드 (JSONException 해결 버전)
     */
    private void sendErrorResponse(HttpServletResponse response, String code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        try {

            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("status", HttpServletResponse.SC_UNAUTHORIZED);
            errorDetails.put("code", code);
            errorDetails.put("message", message);

            // 💡 ObjectMapper로 Map을 JSON String으로 변환
            String responseJson = objectMapper.writeValueAsString(errorDetails);

            response.getWriter().print(responseJson);
            response.getWriter().flush(); // 확실하게 스트림 비우기
        } catch (Exception e) {
            log.error("JSON 응답 생성 중 에러 발생", e);
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}