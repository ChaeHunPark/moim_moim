package com.example.backend.repository;

import com.example.backend.common.exception.CustomException;
import com.example.backend.common.exception.ErrorCode;
import com.example.backend.dto.TokenResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RedisTokenRepository implements TokenRepository {


    private final StringRedisTemplate redisTemplate;

    private static final String RT_PREFIX = "RT:";
    private static final String BL_PREFIX = "BL:";

    @Override
    public void saveRefreshToken(String email, TokenResponseDto tokenDto) {
        redisTemplate.opsForValue().set(
                RT_PREFIX + email,
                tokenDto.getRefreshToken(),
                tokenDto.getRefreshTokenExpirationTime(),
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public void validateRefreshToken(String email, String refreshToken) {
        String savedToken = redisTemplate.opsForValue().get(RT_PREFIX + email);

        if (ObjectUtils.isEmpty(savedToken) || !savedToken.equals(refreshToken)) {
            // 토큰 탈취 가능성이 있으므로 저장된 토큰 삭제 후 예외 발생
            deleteRefreshToken(email);
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        }
    }

    @Override
    public void deleteRefreshToken(String email) {
        redisTemplate.delete(RT_PREFIX + email);
    }

    @Override
    public void addToBlacklist(String accessToken, Long expiration) {
        redisTemplate.opsForValue().set(
                BL_PREFIX + accessToken,
                "logout",
                expiration,
                TimeUnit.MILLISECONDS
        );
    }
}
