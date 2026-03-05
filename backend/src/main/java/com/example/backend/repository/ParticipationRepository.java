package com.example.backend.repository;

import com.example.backend.entity.MeetingPost;
import com.example.backend.entity.Member;
import com.example.backend.entity.Participation;
import com.example.backend.enums.ParticipationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    // 특정 모임에 특정 유저가 이미 참여 중인지 확인하는 메소드 (중복 참여 방지용)
    boolean existsByMemberIdAndMeetingPostId(Long memberId, Long meetingPostId);

    // 특정 모임에서 승인(APPROVED)된 참여자 수 조회
    long countByMeetingPostAndStatus(MeetingPost meetingPost, ParticipationStatus status);
}
