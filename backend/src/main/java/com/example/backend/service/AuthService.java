package com.example.backend.service;

import com.example.backend.common.exception.CustomException;
import com.example.backend.common.exception.ErrorCode;
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

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));


        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        TokenResponseDto tokenDto = jwtTokenProvider.createTokenSet(member.getId(), email, member.getRole().name());

        // Refresh Token Redis 저장
        redisTemplate.opsForValue().set(
                REDIS_RT_PREFIX + email,
                tokenDto.getRefreshToken(),
                tokenDto.getRefreshTokenExpirationTime(),
                TimeUnit.MILLISECONDS
        );

        return tokenDto;
    }

    @Transactional
    public void register(RegisterRequest request) {

        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        Region region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new CustomException(ErrorCode.REGION_NOT_FOUND));

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

        // 1. Refresh Token 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        // 2. 이메일 추출 및 Redis 저장값 확인
        String email = jwtTokenProvider.getUserEmail(refreshToken);
        String savedToken = redisTemplate.opsForValue().get(REDIS_RT_PREFIX + email);

        // 3. 보안 및 만료 체크 (Edge Cases)
        if (ObjectUtils.isEmpty(savedToken) || !savedToken.equals(refreshToken)) {
            redisTemplate.delete(REDIS_RT_PREFIX + email);
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        }

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

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

        return newTokenSet;
    }

    @Transactional
    public void logout(String accessToken) {

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
    }
}