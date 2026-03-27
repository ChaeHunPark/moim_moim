package com.example.backend.controller;

import com.example.backend.dto.MeetingSummaryResponse;
import com.example.backend.dto.ParticipationResponse;
import com.example.backend.service.MeetingService;
import com.example.backend.service.ParticipationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mypage") //
@RequiredArgsConstructor
public class MyPageController {

    private final MeetingService meetingService;
    private final ParticipationService participationService;

    /**
     * 내가 만든 모임 목록 조회 (방장 모드)
     */
    @GetMapping("/meetings/created")
    public ResponseEntity<List<MeetingSummaryResponse>> getMyCreatedMeetings(
            Authentication authentication
    ) {
        // JWT 필터에서 저장한 principal 정보 추출
        Long memberId = Long.valueOf(authentication.getName());
        List<MeetingSummaryResponse> myMeetings = meetingService.getMyCreatedMeetings(memberId);

        return ResponseEntity.status(HttpStatus.OK).body(myMeetings);
    }

    /**
     * 💡 2. 특정 모임의 신청자 명단 조회 (추가)
     * GET /api/participation/meeting/{postId}/participants
     */
    @GetMapping("/meeting/{postId}/participants")
    public ResponseEntity<List<ParticipationResponse>> getParticipants(
            @PathVariable("postId") Long postId,
            Authentication authentication
    ) {
        Long hostId = Long.valueOf(authentication.getName());
        List<ParticipationResponse> responses = participationService.getParticipants(postId, hostId);

        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    /**
     * 내가 신청한 모임 목록 조회 (참여자 모드)
     */
    @GetMapping("/meetings/applied")
    public ResponseEntity<List<MeetingSummaryResponse>> getMyAppliedMeetings(
            Authentication authentication
    ) {
        Long memberId = Long.valueOf(authentication.getName());
        List<MeetingSummaryResponse> appliedMeetings = meetingService.getMyAppliedMeetings(memberId);

        return ResponseEntity.status(HttpStatus.OK).body(appliedMeetings);
    }
}
