package com.example.backend.service;

import com.example.backend.dto.ParticipationRequestDto;
import com.example.backend.entity.MeetingPost;
import com.example.backend.entity.Member;
import com.example.backend.entity.Participation;
import com.example.backend.enums.ParticipationRole;
import com.example.backend.enums.ParticipationStatus;
import com.example.backend.repository.MeetingPostRepository;
import com.example.backend.repository.MemberRepository;
import com.example.backend.repository.ParticipationRepository;
import jakarta.persistence.EntityNotFoundException;
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
                .orElseThrow(() -> new EntityNotFoundException("해당 모임 게시글을 찾을 수 없습니다."));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("해당 회원을 찾을 수 없습니다."));

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
        // 본인이 작성한 글인지 체크 (주최자는 신청 불필요)
        if (meetingPost.getCreator().getId().equals(memberId)) {
            throw new IllegalStateException("모임 주최자는 본인의 모임에 신청할 수 없습니다.");
        }

        // 중복 신청 체크
        if (participationRepository.existsByMemberIdAndMeetingPostId(memberId, meetingPost.getId())) {
            throw new IllegalStateException("이미 신청 중이거나 참여가 완료된 모임입니다.");
        }

        // 정원 체크 (ACCEPTED 상태인 인원만 확인)
        long acceptedCount = participationRepository.countByMeetingPostAndStatus(
                meetingPost, ParticipationStatus.ACCEPTED);

        if (acceptedCount >= meetingPost.getCapacity()) {
            throw new IllegalStateException("모임 정원이 가득 차서 더 이상 신청할 수 없습니다.");
        }
    }
}
