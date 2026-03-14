package com.example.backend.common.security;


import com.example.backend.common.exception.CustomJwtException;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private String validAccessToken;
    private final String secretKeyString = "test-secret-key-at-least-32-characters-long-for-hs256";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();

        // 1. JwtTokenProvider 내부의 'key' 필드에 SecretKey 객체 주입
        SecretKey secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
        ReflectionTestUtils.setField(jwtTokenProvider, "key", secretKey);

        // 2. 테스트에 사용할 정상 토큰 미리 생성 (ID와 Role을 반드시 포함)
        validAccessToken = jwtTokenProvider.createTestToken(1L, "test@example.com", "ROLE_USER", 1000L * 60 * 60);
    }

    @Test
    @DisplayName("정상적인 토큰으로 인증 객체(Authentication)를 생성한다.")
    void getAuthentication_Success() {
        // when
        Authentication authentication = jwtTokenProvider.getAuthentication(validAccessToken);

        // then
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(1L); // Principal에 ID가 들어갔는지 확인
        assertThat(authentication.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("권한 정보가 없는 토큰은 인증에 실패한다.")
    void getAuthentication_NoRole_Fail() {
        // given: 권한(role)이 null인 토큰 생성
        String noRoleToken = jwtTokenProvider.createTestToken(1L, "test@example.com", null, 1000 * 60);

        // then
        assertThrows(RuntimeException.class, () -> jwtTokenProvider.getAuthentication(noRoleToken));
    }

    @Test
    @DisplayName("유효한 토큰의 검증 결과는 true여야 한다.")
    void validateToken_Success() {
        // when
        boolean isValid = jwtTokenProvider.validateToken(validAccessToken);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("변조된 토큰은 validateToken에서 예외가 발생한다.")
    void validateToken_Tampered_Fail() {
        // given: 토큰의 마지막 글자를 바꿔서 변조함
        String tamperedToken = validAccessToken + "abc";

        // then (JwtTokenProvider에서 CustomJwtException을 던지도록 설계됨)
        assertThrows(RuntimeException.class, () -> jwtTokenProvider.validateToken(tamperedToken));
    }

    @Test
    @DisplayName("토큰에서 유저 이메일을 추출한다.")
    void getUserEmail_Success() {
        // when
        String email = jwtTokenProvider.getUserEmail(validAccessToken);

        // then
        assertThat(email).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("토큰에서 유저 ID를 추출한다.")
    void getMemberId_Success() {
        // when
        Long memberId = jwtTokenProvider.getMemberId(validAccessToken);

        // then
        assertThat(memberId).isEqualTo(1L);
    }

    @Test
    @DisplayName("인증 실패: 권한 정보(role)가 없는 토큰인 경우 RuntimeException이 발생한다.")
    void getAuthentication_NoRole_ThrowsException() {
        // given: role을 null로 설정하여 토큰 생성
        String noRoleToken = jwtTokenProvider.createTestToken(1L, "user@test.com", null, 1000 * 60);

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            jwtTokenProvider.getAuthentication(noRoleToken);
        });
        assertThat(exception.getMessage()).contains("권한 정보가 없는 토큰입니다.");
    }

    @Test
    @DisplayName("인증 실패: 사용자 식별값(id)이 없는 토큰인 경우 RuntimeException이 발생한다.")
    void getAuthentication_NoId_ThrowsException() {
        // given: id를 null로 설정하여 토큰 생성
        String noIdToken = jwtTokenProvider.createTestToken(null, "user@test.com", "ROLE_USER", 1000 * 60);

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            jwtTokenProvider.getAuthentication(noIdToken);
        });
        assertThat(exception.getMessage()).contains("식별 정보가 없는 토큰입니다.");
    }

    @Test
    @DisplayName("검증 실패: 만료된 토큰을 검증하면 CustomJwtException(EXPIRED_TOKEN)이 발생한다.")
    void validateToken_Expired_ThrowsCustomException() {
        // given: 유효 기간을 -1ms로 설정하여 이미 만료된 토큰 생성
        String expiredToken = jwtTokenProvider.createTestToken(1L, "user@test.com", "ROLE_USER", -1L);

        // when & then
        CustomJwtException exception = assertThrows(CustomJwtException.class, () -> {
            jwtTokenProvider.validateToken(expiredToken);
        });
        assertThat(exception.getMessage()).contains("로그인 세션이 만료되었습니다");
    }

    @Test
    @DisplayName("만료된 토큰이라도 parseClaims를 통해 내부 정보를 꺼낼 수 있어야 한다.")
    void parseClaims_EvenIfExpired_ReturnsClaims() {
        // given
        String email = "expired-user@test.com";
        String expiredToken = jwtTokenProvider.createTestToken(1L, email, "ROLE_USER", -1L);

        // when: getUserEmail은 내부적으로 parseClaims를 호출함
        String extractedEmail = jwtTokenProvider.getUserEmail(expiredToken);

        // then
        assertThat(extractedEmail).isEqualTo(email);
    }
}