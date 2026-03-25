package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.service.MeetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<MeetingCreateResponse> createMeeting(
            @Valid @RequestBody MeetingPostCreateRequest request,
            @AuthenticationPrincipal Long memberId // JWT 필터에서 저장한 Principal (Member ID)
    ) {
        // 1. 서비스 호출을 통해 모임 생성 및 방장 등록 로직 수행
        Long savedMeetingId = meetingService.createMeeting(request, memberId);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MeetingCreateResponse(savedMeetingId));
    }

    /**
     * 모임 상세 조회 API
     * @param id 조회할 모임의 PK
     * @return 모임 상세 정보 (MeetingDetailResponse)
     */
    @GetMapping("/{id}")
    public ResponseEntity<MeetingDetailResponse> getMeetingDetail(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal Long memberId) {

        MeetingDetailResponse response = meetingService.getMeetingDetail(id,memberId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<List<MeetingListResponse>> getAllMeetings(
            @RequestParam(name = "sortBy", required = false, defaultValue = "latest") String sortBy,
            @RequestParam(name = "categoryId", required = false) Long categoryId) {

        log.info("목록 조회 요청: 정렬={}, 카테고리={}", sortBy, categoryId);
        List<MeetingListResponse> responses = meetingService.getAllMeetings(sortBy, categoryId);

        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    /**
     * 모임 수정 API
     * PUT /api/meetings/{meetingId}
     */
    @PutMapping("/{meetingId}")
    public ResponseEntity<Void> updateMeeting(
            @PathVariable("meetingId") Long meetingId,
            @Valid @RequestBody MeetingPostUpdateRequest request,
            @AuthenticationPrincipal Long memberId // 필터에서 저장된 유저 ID 주입
    ) {
        meetingService.updateMeeting(meetingId, request, memberId);
        return ResponseEntity.ok().build(); // 200 OK
    }

    /**
     * 모임 삭제 API
     * DELETE /api/meetings/{meetingId}
     */
    @DeleteMapping("/{meetingId}")
    public ResponseEntity<Void> deleteMeeting(
            @PathVariable("meetingId") Long meetingId,
            @AuthenticationPrincipal Long memberId
    ) {
        meetingService.deleteMeeting(meetingId, memberId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
