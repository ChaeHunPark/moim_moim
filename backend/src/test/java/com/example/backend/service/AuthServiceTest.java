package com.example.backend.service;

import com.example.backend.common.exception.CustomException;
import com.example.backend.common.exception.ErrorCode;
import com.example.backend.common.security.JwtTokenProvider;
import com.example.backend.dto.RegisterRequest;
import com.example.backend.dto.TokenResponseDto;
import com.example.backend.entity.Member;
import com.example.backend.entity.Region;
import com.example.backend.enums.Role;
import com.example.backend.repository.MemberRepository;
import com.example.backend.repository.RegionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private RegionRepository regionRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    // Redis opsForValue() 반복 모킹을 위한 헬퍼 메소드
    private void mockRedis() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("로그인 성공 - 토큰을 반환하고 Redis에 리프레시 토큰을 저장한다")
    void login_success() {
        // [Given]
        String email = "test@test.com";
        String password = "password123";
        Member member = Member.builder()
                .id(1L).email(email).password("encoded").role(Role.ROLE_USER).build();
        TokenResponseDto tokenDto = TokenResponseDto.builder()
                .accessToken("at").refreshToken("rt").refreshTokenExpirationTime(3600L).build();

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(password, member.getPassword())).thenReturn(true);
        when(jwtTokenProvider.createTokenSet(1L, email, "ROLE_USER")).thenReturn(tokenDto);
        mockRedis();

        // [When]
        TokenResponseDto result = authService.login(email, password);

        // [Then]
        assertThat(result.getAccessToken()).isEqualTo("at");
        verify(valueOperations).set(eq("RT:" + email), eq("rt"), anyLong(), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("회원가입 실패 - 존재하지 않는 지역 ID인 경우 REGION_NOT_FOUND 예외 발생")
    void register_fail_invalid_region() {
        // [Given]
        RegisterRequest request = new RegisterRequest();
        ReflectionTestUtils.setField(request, "email", "test@test.com");
        ReflectionTestUtils.setField(request, "regionId", 999L);

        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(regionRepository.findById(999L)).thenReturn(Optional.empty());

        // [When & Then]
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.REGION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("재발급 실패 - Redis의 토큰과 일치하지 않으면 EXPIRED_TOKEN 예외가 발생한다")
    void reissue_fail_token_mismatch() {
        // [Given]
        String inputToken = "wrong-token";
        String email = "test@test.com";
        String savedToken = "original-token";

        when(jwtTokenProvider.validateToken(inputToken)).thenReturn(true);
        when(jwtTokenProvider.getUserEmail(inputToken)).thenReturn(email);
        mockRedis();
        when(valueOperations.get("RT:" + email)).thenReturn(savedToken);

        // [When & Then]
        assertThatThrownBy(() -> authService.reissue(inputToken))
                .isInstanceOf(CustomException.class)
                // 💡 서비스 로직에 맞춰 EXPIRED_TOKEN 메시지로 검증
                .hasMessageContaining(ErrorCode.EXPIRED_TOKEN.getMessage());

        // 보안 조치로 Redis 데이터가 삭제되었는지 확인
        verify(redisTemplate).delete("RT:" + email);
    }

    @Test
    @DisplayName("로그아웃 성공 - Redis에서 RT를 삭제하고 AT를 블랙리스트에 등록한다")
    void logout_success() {
        // [Given]
        String accessToken = "access-token";
        String email = "test@test.com";
        when(jwtTokenProvider.getUserEmail(accessToken)).thenReturn(email);
        when(jwtTokenProvider.getExpiration(accessToken)).thenReturn(3600L);
        mockRedis();

        // [When]
        authService.logout(accessToken);

        // [Then]
        verify(redisTemplate).delete("RT:" + email);
        verify(valueOperations).set(eq("BL:" + accessToken), eq("logout"), eq(3600L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호가 일치하지 않으면 INVALID_PASSWORD 예외 발생")
    void login_fail_invalid_password() {
        // [Given]
        String email = "test@test.com";
        Member member = Member.builder().email(email).password("encoded").build();

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // [When & Then]
        assertThatThrownBy(() -> authService.login(email, "wrong-password"))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.INVALID_PASSWORD.getMessage());
    }
}

