package com.example.backend.service;

import com.example.backend.common.security.JwtTokenProvider;
import com.example.backend.dto.RegisterRequest;
import com.example.backend.dto.TokenResponseDto;
import com.example.backend.entity.Member;
import com.example.backend.entity.Region;
import com.example.backend.enums.MemberStatus;
import com.example.backend.enums.Role;
import com.example.backend.repository.MemberRepository;
import com.example.backend.repository.RegionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(readOnly = true)
@Slf4j // 로그 기록을 위한 어노테이션
public class AuthService {

    private final MemberRepository memberRepository;
    private final RegionRepository regionRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    private static final String REDIS_RT_PREFIX = "RT:";

    public AuthService(MemberRepository memberRepository, RegionRepository regionRepository, JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder, StringRedisTemplate redisTemplate) {
        this.memberRepository = memberRepository;
        this.regionRepository = regionRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public TokenResponseDto login(String email, String password) {
        log.info("로그인 시도 - Email: {}", email);

        // 1. 사용자 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("로그인 실패 - 존재하지 않는 이메일: {}", email);
                    return new RuntimeException("가입되지 않은 이메일입니다.");
                });

        // 2. 비밀번호 확인
        if (!passwordEncoder.matches(password, member.getPassword())) {
            log.warn("로그인 실패 - 비밀번호 불일치: {}", email);
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        String role = member.getRole().name();
        TokenResponseDto tokenDto = jwtTokenProvider.createTokenSet(email, role);

        // 5. Redis에 Refresh Token 저장
        redisTemplate.opsForValue().set(
                REDIS_RT_PREFIX + email,
                tokenDto.getRefreshToken(),
                tokenDto.getRefreshTokenExpirationTime(),
                TimeUnit.MILLISECONDS
        );

        log.info("로그인 성공 - Email: {}, Role: {}", email, role);
        return tokenDto;
    }

    @Transactional
    public void register(RegisterRequest request) {
        log.info("회원가입 시도 - Email: {}, Nickname: {}", request.getEmail(), request.getNickname());

        if (memberRepository.existsByEmail(request.getEmail())) {
            log.warn("회원가입 실패 - 중복 이메일: {}", request.getEmail());
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        Region region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> {
                    log.error("회원가입 실패 - 존재하지 않는 지역 ID: {}", request.getRegionId());
                    return new IllegalArgumentException("존재하지 않는 지역입니다.");
                });

        Member member = Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .age(request.getAge())
                .introduction(request.getBio())
                .region(region)
                .role(Role.ROLE_USER)
                .status(MemberStatus.ACTIVE)
                .points(0)
                .level(1)
                .build();

        memberRepository.save(member);
        log.info("회원가입 완료 - Email: {}", request.getEmail());
    }

    @Transactional
    public TokenResponseDto reissue(String refreshToken) {
        log.info("토큰 재발급 시도");

        // 1. 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.warn("재발급 실패 - 유효하지 않은 리프레시 토큰");
            throw new RuntimeException("리프레시 토큰이 유효하지 않습니다.");
        }

        String email = jwtTokenProvider.getUserEmail(refreshToken);
        String savedToken = redisTemplate.opsForValue().get(REDIS_RT_PREFIX + email);

        // 3. 보안 체크 (중요: 탈취 시나리오 로그)
        if (savedToken == null) {
            log.warn("재발급 실패 - Redis에 저장된 토큰이 없음 (만료 혹은 로그아웃 상태): {}", email);
            throw new RuntimeException("토큰 정보가 만료되었습니다.");
        }

        if (!savedToken.equals(refreshToken)) {
            log.error("보안 경고 - 리프레시 토큰 불일치! 토큰 탈취 의심: {}", email);
            redisTemplate.delete(REDIS_RT_PREFIX + email); // 보안상 즉시 삭제
            throw new RuntimeException("토큰 정보가 일치하지 않습니다.");
        }

        // 실제 권한 조회가 필요하다면 DB 조회가 필요하겠지만, 여기서는 USER로 고정 혹은 토큰에서 추출
        TokenResponseDto newTokenSet = jwtTokenProvider.createTokenSet(email, "ROLE_USER");

        redisTemplate.opsForValue().set(
                REDIS_RT_PREFIX + email,
                newTokenSet.getRefreshToken(),
                newTokenSet.getRefreshTokenExpirationTime(),
                TimeUnit.MILLISECONDS
        );

        log.info("토큰 재발급 완료 - Email: {}", email);
        return newTokenSet;
    }

    @Transactional
    public void logout(String accessToken) {
        if (!jwtTokenProvider.validateToken(accessToken)) {
            log.warn("로그아웃 시도 실패 - 유효하지 않은 토큰");
            throw new RuntimeException("유효하지 않은 토큰입니다.");
        }

        String email = jwtTokenProvider.getUserEmail(accessToken);
        Long expiration = jwtTokenProvider.getExpiration(accessToken);

        // Redis 처리
        redisTemplate.delete(REDIS_RT_PREFIX + email);
        redisTemplate.opsForValue().set(
                "BL:" + accessToken,
                "logout",
                expiration,
                TimeUnit.MILLISECONDS
        );

        log.info("로그아웃 성공 - Email: {}, 블랙리스트 만료시간: {}ms", email, expiration);
    }
}