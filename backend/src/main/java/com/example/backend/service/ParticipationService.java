package com.example.backend.service;

import com.example.backend.common.exception.CustomException;
import com.example.backend.common.exception.ErrorCode;
import com.example.backend.dto.ParticipationRequestDto;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationService {

    private final ParticipationRepository participationRepository;
    private final MeetingPostRepository meetingPostRepository; // 게시글 조회용
    private final MemberRepository memberRepository;

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

        return participationRepository.save(participation).getId();
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
}
