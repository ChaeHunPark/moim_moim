package com.example.backend.repository;

import com.example.backend.entity.Participation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    // 특정 모임에 특정 유저가 이미 참여 중인지 확인하는 메소드 (중복 참여 방지용)
    boolean existsByMemberIdAndMeetingPostId(Long memberId, Long meetingPostId);
}
