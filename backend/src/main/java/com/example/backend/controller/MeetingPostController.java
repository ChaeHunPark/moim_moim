package com.example.backend.controller;

import com.example.backend.dto.MeetingDetailResponse;
import com.example.backend.dto.MeetingPostCreateRequest;
import com.example.backend.service.MeetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
@Slf4j
public class MeetingPostController {

    private final MeetingService meetingService;

    /**
     * 새로운 모임 생성 API
     * POST /api/meetings
     */
    @PostMapping
    public ResponseEntity<Long> createMeeting(
            @Valid @RequestBody MeetingPostCreateRequest request,
            @AuthenticationPrincipal Long memberId // JWT 필터에서 저장한 Principal (Member ID)
    ) {
        // 1. 서비스 호출을 통해 모임 생성 및 방장 등록 로직 수행
        Long savedMeetingId = meetingService.createMeeting(request, memberId);

        // 2. 생성된 모임 ID와 함께 201 Created 응답 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(savedMeetingId);
    }

    /**
     * 모임 상세 조회 API
     * @param id 조회할 모임의 PK
     * @return 모임 상세 정보 (MeetingDetailResponse)
     */
    @GetMapping("/{id}")
    public ResponseEntity<MeetingDetailResponse> getMeetingDetail(@PathVariable("id") Long id) {
        log.info("모임 상세 조회 요청 - ID: {}", id);

        // 서비스에서 조회수 증가 로직과 데이터 조회 로직이 처리됩니다.
        MeetingDetailResponse response = meetingService.getMeetingDetail(id);

        return ResponseEntity.ok(response);
    }
}
