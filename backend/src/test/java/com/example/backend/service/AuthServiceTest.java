package com.example.backend.service;

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

    @Test
    @DisplayName("회원가입 성공 - 유효한 정보로 가입 시 데이터가 저장되어야 한다")
    void register_success() {
        // [Given] 1. DTO 준비 (RegisterRequest는 아직 Builder가 없으므로 Reflection 유지)
        RegisterRequest request = new RegisterRequest();
        ReflectionTestUtils.setField(request, "email", "test@test.com");
        ReflectionTestUtils.setField(request, "password", "password123");
        ReflectionTestUtils.setField(request, "nickname", "테스터");
        ReflectionTestUtils.setField(request, "age", 25);
        ReflectionTestUtils.setField(request, "regionId", 1L);
        ReflectionTestUtils.setField(request, "bio", "반갑습니다");

        // [Given] 2. Region 엔티티 준비 (확인해주신 @Builder 사용!)
        Region region = Region.builder()
                .id(1L)
                .name("서울")
                .depth(1)
                .build();

        // [Given] 3. Mock 동작 정의
        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(regionRepository.findById(1L)).thenReturn(Optional.of(region));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");

        // [When] 실행
        authService.register(request);

        // [Then] 검증
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 존재하지 않는 지역 ID인 경우 예외 발생")
    void register_fail_invalid_region() {
        // [Given]
        RegisterRequest request = new RegisterRequest();
        ReflectionTestUtils.setField(request, "regionId", 999L);

        when(memberRepository.existsByEmail(any())).thenReturn(false);
        when(regionRepository.findById(999L)).thenReturn(Optional.empty());

        // [When & Then]
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 지역입니다.");
    }

    @Test
    @DisplayName("로그인 성공 - 올바른 정보 입력 시 토큰을 반환하고 Redis에 저장한다")
    void login_success() {
        // [Given] 1. 필요한 데이터 준비
        String email = "test@test.com";
        String password = "password123";
        Member member = Member.builder()
                .id(1L)
                .email(email)
                .password("encoded_password") // 암호화된 비밀번호
                .role(Role.ROLE_USER)
                .build();

        TokenResponseDto tokenDto = TokenResponseDto.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .refreshTokenExpirationTime(3600L)
                .build();

        // [Given] 2. Mock 객체 동작 정의
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(password, member.getPassword())).thenReturn(true);
        when(jwtTokenProvider.createTokenSet(anyLong(), anyString(), anyString())).thenReturn(tokenDto);

        // Redis 모킹: opsForValue()가 valueOperations를 반환하도록 설정
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // [When] 실행
        TokenResponseDto result = authService.login(email, password);

        // [Then] 검증
        assertThat(result.getAccessToken()).isEqualTo("access-token");

        // Redis에 리프레시 토큰이 잘 저장되었는지 확인
        verify(valueOperations, times(1)).set(
                eq("RT:" + email),
                eq("refresh-token"),
                anyLong(),
                eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호가 일치하지 않으면 예외가 발생한다")
    void login_fail_invalid_password() {
        // [Given]
        String email = "test@test.com";
        String password = "wrong_password";
        Member member = Member.builder()
                .email(email)
                .password("encoded_password")
                .build();

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(password, member.getPassword())).thenReturn(false);

        // [When & Then]
        assertThatThrownBy(() -> authService.login(email, password))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("로그아웃 성공 - Redis에서 리프레시 토큰을 삭제하고 액세스 토큰을 블랙리스트에 올린다")
    void logout_success() {
        // [Given] 1. 필요한 데이터 준비
        String accessToken = "valid-access-token";
        String email = "test@test.com";
        Long expiration = 3600L;

        // JwtTokenProvider 동작 모킹
        when(jwtTokenProvider.validateToken(accessToken)).thenReturn(true);
        when(jwtTokenProvider.getUserEmail(accessToken)).thenReturn(email);
        when(jwtTokenProvider.getExpiration(accessToken)).thenReturn(expiration);

        // Redis opsForValue() 모킹
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // [When] 실행
        authService.logout(accessToken);

        // [Then] 검증
        // 1. RT(Refresh Token) 삭제 확인
        verify(redisTemplate, times(1)).delete("RT:" + email);

        // 2. BL(BlackList) 등록 확인
        verify(valueOperations, times(1)).set(
                eq("BL:" + accessToken),
                eq("logout"),
                eq(expiration),
                eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    @DisplayName("로그아웃 실패 - 유효하지 않은 토큰으로 로그아웃 시도 시 예외 발생")
    void logout_fail_invalid_token() {
        // [Given]
        String invalidToken = "invalid-token";
        when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);

        // [When & Then]
        assertThatThrownBy(() -> authService.logout(invalidToken))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("유효하지 않은 토큰입니다.");
    }

    @Test
    @DisplayName("재발급 성공 - 유효한 리프레시 토큰이면 새로운 토큰 세트를 반환한다")
    void reissue_success() {
        // [Given] 1. 필요한 데이터 준비
        String oldRefreshToken = "old-refresh-token";
        String email = "test@test.com";

        Member member = Member.builder()
                .id(1L)
                .email(email)
                .role(Role.ROLE_USER)
                .build();

        TokenResponseDto newTokenSet = TokenResponseDto.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .refreshTokenExpirationTime(3600L)
                .build();

        // [Given] 2. Mock 동작 정의
        when(jwtTokenProvider.validateToken(oldRefreshToken)).thenReturn(true);
        when(jwtTokenProvider.getUserEmail(oldRefreshToken)).thenReturn(email);

        // Redis에서 기존 토큰 꺼내오기
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("RT:" + email)).thenReturn(oldRefreshToken);

        // DB 조회 및 새 토큰 생성
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(jwtTokenProvider.createTokenSet(member.getId(), email, member.getRole().name())).thenReturn(newTokenSet);

        // [When] 실행
        TokenResponseDto result = authService.reissue(oldRefreshToken);

        // [Then] 검증
        assertThat(result.getAccessToken()).isEqualTo("new-access-token");
        assertThat(result.getRefreshToken()).isEqualTo("new-refresh-token");

        // Redis 갱신 확인 (새로운 리프레시 토큰으로 저장되었는가)
        verify(valueOperations, times(1)).set(
                eq("RT:" + email),
                eq("new-refresh-token"),
                anyLong(),
                eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    @DisplayName("재발급 실패 - Redis의 토큰과 일치하지 않으면 보안 위협으로 간주하고 삭제한다")
    void reissue_fail_token_mismatch() {
        // [Given]
        String stolenToken = "stolen-token";
        String email = "test@test.com";
        String savedToken = "actual-token-in-redis";

        when(jwtTokenProvider.validateToken(stolenToken)).thenReturn(true);
        when(jwtTokenProvider.getUserEmail(stolenToken)).thenReturn(email);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("RT:" + email)).thenReturn(savedToken);

        // [When & Then]
        assertThatThrownBy(() -> authService.reissue(stolenToken))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("토큰 정보가 일치하지 않습니다.");

        // 보안 조치: 기존 Redis 토큰이 즉시 삭제되었는지 확인
        verify(redisTemplate, times(1)).delete("RT:" + email);
    }

}

