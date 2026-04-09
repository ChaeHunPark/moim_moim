package com.example.backend.repository;

import com.example.backend.dto.TokenResponseDto;

import java.util.Optional;

public interface TokenRepository {
    void saveRefreshToken(String email, TokenResponseDto tokenDto);
    void validateRefreshToken(String email, String refreshToken);
    void deleteRefreshToken(String email);
    void addToBlacklist(String accessToken, Long expiration);
}
