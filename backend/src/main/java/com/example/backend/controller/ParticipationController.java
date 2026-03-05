package com.example.backend.controller;

import com.example.backend.dto.ParticipationRequestDto;
import com.example.backend.service.ParticipationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/participation")
@RequiredArgsConstructor
public class ParticipationController {

    private final ParticipationService participationService;

    @PostMapping("/apply")
    public ResponseEntity<Long> apply(@Valid @RequestBody ParticipationRequestDto requestDto,
                                      Authentication authentication) {
        // JWT 필터에서 저장한 principal 정보 추출
        Long memberId = Long.valueOf(authentication.getName());
        // 식별자(Email 등)를 서비스로 넘깁니다.
        Long participationId = participationService.applyForMeeting(requestDto, memberId);

        return ResponseEntity.status(HttpStatus.CREATED).body(participationId);
    }
}
