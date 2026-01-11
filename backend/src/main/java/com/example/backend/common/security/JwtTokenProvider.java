package com.example.backend.common.security;

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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private Key key;
    private final long tokenValidityInMilliseconds = 1000L * 60 * 60; // 1시간

    @PostConstruct
    protected void init() {
        // 0.11 버전: HMAC-SHA 키 생성
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // 토큰 생성
    public String createToken(String email, String role) {
        // 1. Claims 객체 생성 및 정보 삽입
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role); // 여기에 "ROLE_USER" 문자열이

        Date now = new Date();
        Date validity = new Date(now.getTime() + 3600000); // 1시간

        // 2. 토큰 빌더 구성
        return Jwts.builder()
                .setClaims(claims)      // 위에서 만든 role 포함 claims 세팅
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 인증 객체 생성 (중요: org.springframework.security.core.Authentication 사용)
    public Authentication getAuthentication(String token) {
// 1. 토큰 파싱하여 Claims 추출
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        // 2. Claims에서 "role" 꺼내기 (객체가 없으면 예외 처리)
        Object roleClaim = claims.get("role");
        if (roleClaim == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        String roleName = roleClaim.toString();

        // 3. 시큐리티 권한 객체 생성
        Collection<? extends GrantedAuthority> authorities =
                Collections.singletonList(new SimpleGrantedAuthority(roleName));

        // 4. 최종 인증 객체 반환 (Principal 자리에 email 주입)
        return new UsernamePasswordAuthenticationToken(claims.getSubject(), "", authorities);
    }

    // 토큰 유효성 검사 (0.11 버전 parserBuilder 사용)
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }
}