package com.example.backend.service;

import com.example.backend.common.exception.CustomException;
import com.example.backend.common.exception.ErrorCode;
import com.example.backend.dto.ParticipationRequestDto;
import com.example.backend.dto.ParticipationResponse;
import com.example.backend.entity.MeetingPost;
import com.example.backend.entity.Member;
import com.example.backend.entity.Participation;
import com.example.backend.enums.ParticipationRole;
import com.example.backend.enums.ParticipationStatus;
import com.example.backend.repository.MeetingPostRepository;
import com.example.backend.repository.MemberRepository;
import com.example.backend.repository.ParticipationRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationService {

    private final ParticipationRepository participationRepository;
    private final MeetingPostRepository meetingPostRepository; // 게시글 조회용
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    @Transactional
    public Long applyForMeeting(ParticipationRequestDto requestDto, Long memberId) {
        // 1. 엔티티 존재 여부 확인
        MeetingPost meetingPost = meetingPostRepository.findById(requestDto.getMeetingPostId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEETING_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 2. 검증 로직
        validateApplication(meetingPost, memberId);

        // 3. 참여 정보 생성 및 저장
        Participation participation = Participation.builder()
                .member(member)
                .meetingPost(meetingPost)
                .role(ParticipationRole.PARTICIPANT)
                .status(ParticipationStatus.APPLIED) // 초기 상태: 신청완료
                .joinReason(requestDto.getJoinReason())
                .build();

        participationRepository.save(participation);

        // 3. 🔔 모임장(Host)에게 알림 생성
        notificationService.createNotification(
                meetingPost.getCreator(), // 알림 수신자: 모임 만든 사람
                "[" + meetingPost.getTitle() + "] 모임에 새로운 참여 신청이 도착했습니다! 📩",
                "/mypage?tab=hosted" // 모임장이 확인해야 할 페이지
        );


        return participation.getId();
    }

    private void validateApplication(MeetingPost meetingPost, Long memberId) {
        if (meetingPost.getCreator().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACCESS); // 주최자 신청 불가
        }

        if (participationRepository.existsByMemberIdAndMeetingPostId(memberId, meetingPost.getId())) {
            throw new CustomException(ErrorCode.ALREADY_PARTICIPATED);
        }

        long acceptedCount = participationRepository.countByMeetingPostAndStatus(meetingPost, ParticipationStatus.ACCEPTED);
        if (acceptedCount >= meetingPost.getCapacity()) {
            throw new CustomException(ErrorCode.MEETING_FULL);
        }
    }

    /**
     * 3. 특정 모임의 신청자 명단 조회 (방장 권한 확인 필수)
     */
    @Transactional(readOnly = true)
    public List<ParticipationResponse> getParticipants(Long postId, Long hostId) {
        MeetingPost post = meetingPostRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEETING_NOT_FOUND));

        // 💡 방장 권한 체크
        if (!post.getCreator().getId().equals(hostId)) {
            throw new CustomException(ErrorCode.NOT_AUTHORIZED_PARTICIPATION);
        }

        return participationRepository.findAllByMeetingPostId(postId).stream()
                .map(ParticipationResponse::from)
                .collect(Collectors.toList());
    }



    /**
     * 4. 참여 신청 승인/거절 처리
     */
    @Transactional
    public Long updateParticipationStatus(Long participationId, String statusStr, Long hostId) {
        Participation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new CustomException(ErrorCode.PARTICIPATION_NOT_FOUND));

        // 💡 권한 체크
        if (!participation.getMeetingPost().getCreator().getId().equals(hostId)) {
            throw new CustomException(ErrorCode.NOT_AUTHORIZED_PARTICIPATION);
        }

        try {
            ParticipationStatus newStatus = ParticipationStatus.valueOf(statusStr.toUpperCase());
            participation.updateStatus(newStatus);

            if (newStatus == ParticipationStatus.ACCEPTED) {
                MeetingPost post = participation.getMeetingPost();
                post.addParticipant();
                // 알림 생성 호출
                notificationService.createNotification(
                        participation.getMember(), // 신청자
                        "[" + participation.getMeetingPost().getTitle() + "] 모임 참여가 승인되었습니다! 🎉",
                        "/mypage?tab=applied"
                );
            }
        } catch (IllegalArgumentException e) {
            // 💡 잘못된 상태 값 (예: ACCEPTED인데 ACSEPTED로 보낸 경우 등)
            throw new CustomException(ErrorCode.INVALID_PARTICIPATION_STATUS);
        }

        return participation.getId();
    }
}
