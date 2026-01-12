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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
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

    private final long ACCESS_TOKEN_VALIDITY = 1000L * 60 * 60; // 1시간
    private final long REFRESH_TOKEN_VALIDITY = 1000L * 60 * 60 * 24 * 7; // 7일

    @PostConstruct
    protected void init() {
        // 0.11 버전: HMAC-SHA 키 생성
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }


    /*
     * 로그인 성공 시 호출, 이메일과 권한을 받아서 access, refresh를 만듬
     * */
    public TokenResponseDto createTokenSet(String email, String role) {
        String accessToken = createToken(email, role, ACCESS_TOKEN_VALIDITY);
        String refreshToken = createToken(email, role, REFRESH_TOKEN_VALIDITY);

        return TokenResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .refreshTokenExpirationTime(REFRESH_TOKEN_VALIDITY)
                .build();
    }

    private String createToken(String email, String role, long validityMillis) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);

        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + validityMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /*
    * 토큰에 있는 유저 정보를 꺼내서 Security가 이해할 수 있는 Auth로 변환
    * */
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        Object roleClaim = claims.get("role");
        if (roleClaim == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        Collection<? extends GrantedAuthority> authorities =
                Collections.singletonList(new SimpleGrantedAuthority(roleClaim.toString()));

        return new UsernamePasswordAuthenticationToken(claims.getSubject(), "", authorities);
    }


    /*
    * 토큰이 유효한지 검사
    * */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.");
        } catch (Exception e) {
            log.error("유효하지 않은 JWT 토큰입니다: {}", e.getMessage());
        }
        return false;
    }

    public String getUserEmail(String token) {
        return parseClaims(token).getSubject();
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public Long getExpiration(String accessToken) {
        // 토큰의 만료 시간 추출
        Date expiration = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(accessToken).getBody().getExpiration();

        // 현재 시간과의 차이 계산
        long now = new Date().getTime();
        return (expiration.getTime() - now);
    }

}