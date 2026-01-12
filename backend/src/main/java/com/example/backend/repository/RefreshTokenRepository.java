package com.example.backend.repository;

import java.util.Optional;

public interface RefreshTokenRepository {
    void save(String userEmail, String refreshToken, long expirationTimeMillis);
    Optional<String> findByEmail(String userEmail);
    void deleteByEmail(String userEmail);
}
