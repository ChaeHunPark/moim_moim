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
import org.springframework.util.ObjectUtils;

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
        log.info("1. 로그인 시도 시작 - Email: {}", email);

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("가입되지 않은 이메일입니다."));

        log.info("2. 사용자 조회 완료: {}", member.getEmail());

        if (!passwordEncoder.matches(password, member.getPassword())) {
            log.warn("3. 비밀번호 불일치: {}", email);
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        log.info("4. 비밀번호 일치 확인");

        TokenResponseDto tokenDto = jwtTokenProvider.createTokenSet(member.getId(), email, member.getRole().name());
        log.info("5. JWT 토큰 생성 완료");

        try {
            redisTemplate.opsForValue().set(
                    REDIS_RT_PREFIX + email,
                    tokenDto.getRefreshToken(),
                    tokenDto.getRefreshTokenExpirationTime(),
                    TimeUnit.MILLISECONDS
            );
            log.info("6. Redis 저장 완료");
        } catch (Exception e) {
            log.error("⚠️ Redis 저장 중 에러 발생: {}", e.getMessage());
            throw e; // 여기서 터지면 401/500의 원인이 됨
        }

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

        // 1. Refresh Token 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.warn("재발급 실패 - 유효하지 않은 리프레시 토큰");
            throw new RuntimeException("리프레시 토큰이 유효하지 않습니다.");
        }

        // 2. 이메일 추출 및 Redis 저장값 확인
        String email = jwtTokenProvider.getUserEmail(refreshToken);
        String savedToken = redisTemplate.opsForValue().get(REDIS_RT_PREFIX + email);

        // 3. 보안 및 만료 체크 (Edge Cases)
        if (ObjectUtils.isEmpty(savedToken)) {
            log.warn("재발급 실패 - Redis에 저장된 토큰이 없음: {}", email);
            throw new RuntimeException("토큰 정보가 만료되었습니다.");
        }

        if (!savedToken.equals(refreshToken)) {
            log.error("보안 경고 - 토큰 불일치! 탈취 의심으로 인한 즉시 삭제: {}", email);
            redisTemplate.delete(REDIS_RT_PREFIX + email);
            throw new RuntimeException("토큰 정보가 일치하지 않습니다.");
        }

        // [핵심 수정] 4. DB 조회를 통해 최신 ID와 권한 정보를 가져옴
        // (재발급 시점에도 정확한 ID를 토큰에 심어주기 위함)
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        // 5. 새로운 토큰 세트 생성 (ID 포함)
        TokenResponseDto newTokenSet = jwtTokenProvider.createTokenSet(
                member.getId(),
                member.getEmail(),
                member.getRole().name()
        );

        // 6. Redis 리프레시 토큰 갱신
        redisTemplate.opsForValue().set(
                REDIS_RT_PREFIX + email,
                newTokenSet.getRefreshToken(),
                newTokenSet.getRefreshTokenExpirationTime(),
                TimeUnit.MILLISECONDS
        );

        log.info("토큰 재발급 완료 - Email: {}, MemberID: {}", email, member.getId());
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