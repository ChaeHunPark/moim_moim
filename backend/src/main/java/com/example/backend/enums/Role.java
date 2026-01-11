package com.example.backend.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    // 스프링 시큐리티는 "ROLE_" 접두사를 기본으로 인식

    ROLE_USER("일반 사용자"),
    ROLE_ADMIN("관리자");

    private final String description;
}
