package com.example.backend.common.security;

import com.example.backend.dto.TokenResponseDto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;
/*
* 토큰 만들고, 해석, 검사 하는 역할만 수행
*
* */


@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private Key key;

    // 토큰 유효 기간 설정 (밀리초 단위)
    private final long ACCESS_TOKEN_VALIDITY = 1000L * 60 * 60; // 1시간 (보안을 위해 짧게 유지)
    private final long REFRESH_TOKEN_VALIDITY = 1000L * 60 * 60 * 24 * 7; // 7일 (재로그인 번거로움 방지)

    /**
     * Bean 생성 후 비밀번호를 이용해 HMAC-SHA 암호화 키를 초기화합니다.
     */
    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * [로그인 전용] 서비스 레이어에서 호출하여 Access/Refresh 토큰 세트를 생성합니다.
     * @param id 유저의 PK (DB ID)
     * @param email 유저 이메일 (Subject)
     * @param role 유저 권한 (ROLE_USER 등)
     */
    public TokenResponseDto createTokenSet(Long id, String email, String role) {
        // 1. Access Token: 모든 정보(ID, Role)를 포함하여 실제 API 요청 시 인증에 사용
        String accessToken = createToken(id, email, role, ACCESS_TOKEN_VALIDITY);

        // 2. Refresh Token: 보안상 PK와 Role을 제외하고 이메일만 포함 (액세스 토큰 재발급용)
        String refreshToken = createToken(null, email, null, REFRESH_TOKEN_VALIDITY);

        return TokenResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .refreshTokenExpirationTime(REFRESH_TOKEN_VALIDITY)
                .build();
    }

    /**
     * [공통] JWT 토큰을 실제로 빌드하는 핵심 로직입니다.
     */
    private String createToken(Long id, String email, String role, long validityMillis) {
        Claims claims = Jwts.claims().setSubject(email);

        // 중요: ID와 Role이 존재할 때만 클레임에 추가 (Refresh Token 생성 시엔 null일 수 있음)
        if (id != null) claims.put("id", id);
        if (role != null) claims.put("role", role);

        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + validityMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * [인증 확인] Filter에서 호출. 토큰 내부 정보를 추출해 스프링 시큐리티의 Authentication 객체로 변환합니다.
     * 여기서 Principal에 '이메일'이 아닌 'Long 타입의 ID'를 넣는 것이 포인트입니다.
     */
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        // [Edge Case] 권한 정보(Role)가 없는 토큰은 차단
        Object roleClaim = claims.get("role");
        if (roleClaim == null) {
            log.error("인증 실패: 권한 정보가 누락된 토큰입니다.");
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // [Edge Case] 식별값(ID)이 없는 토큰은 차단 (구버전 토큰이나 잘못된 접근)
        Object idClaim = claims.get("id");
        if (idClaim == null) {
            log.error("인증 실패: 사용자 ID가 누락된 토큰입니다.");
            throw new RuntimeException("식별 정보가 없는 토큰입니다. 다시 로그인해주세요.");
        }

        // Claims에서 안전하게 정보를 추출하여 Authentication 객체 생성
        Long memberId = Long.valueOf(idClaim.toString());
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(roleClaim.toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        // Credentials(비밀번호)는 null로 처리 (이미 토큰으로 검증됨)
        return new UsernamePasswordAuthenticationToken(memberId, null, authorities);
    }

    /**
     * [검증] 토큰의 변조 여부 및 만료 시간을 체크합니다.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("만료된 토큰입니다.");
        } catch (Exception e) {
            log.error("유효하지 않은 토큰입니다: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 토큰 본문(Claims)을 안전하게 파싱합니다. 만료된 경우에도 정보를 꺼낼 수 있도록 처리합니다.
     */
    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰이라도 재발급 등을 위해 내부 정보(Claims)는 반환함
            return e.getClaims();
        }
    }

    /**
     * [로그아웃용] 토큰의 남은 유효 시간을 계산하여 Redis 블랙리스트 등록 시 사용합니다.
     */
    public Long getExpiration(String accessToken) {
        Date expiration = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(accessToken).getBody().getExpiration();

        long now = new Date().getTime();
        return (expiration.getTime() - now);
    }

    /**
     * 토큰에서 이메일을 추출합니다.
     */
    public String getUserEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 토큰에서 유저 PK(ID)를 직접 추출합니다.
     */
    public Long getMemberId(String token) {
        Claims claims = parseClaims(token);
        Object id = claims.get("id");
        return id != null ? Long.valueOf(id.toString()) : null;
    }
}