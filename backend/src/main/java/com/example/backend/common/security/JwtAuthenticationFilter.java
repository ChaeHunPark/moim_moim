package com.example.backend.common.security;

import lombok.RequiredArgsConstructor;
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

/*
* API 요청은 이필터를 반드시 통과
* */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    private final StringRedisTemplate redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            // [체크] Redis 블랙리스트 확인
            String isLogout = redisTemplate.opsForValue().get("BL:" + token);

            if (ObjectUtils.isEmpty(isLogout)) {
                // [수정] getAuthentication 메서드가 내부적으로 ID(Long)를 Principal에 담도록 구현되어야 함
                Authentication auth = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);

                log.debug("인증 완료 - Principal: {}", auth.getPrincipal());
            } else {
                log.warn("로그아웃된 토큰으로 접근 시도: {}", token);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}