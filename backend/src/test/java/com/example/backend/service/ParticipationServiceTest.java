package com.example.backend.service;

import com.example.backend.dto.ParticipationRequestDto;
import com.example.backend.entity.MeetingPost;
import com.example.backend.entity.Member;
import com.example.backend.entity.Participation;
import com.example.backend.repository.MeetingPostRepository;
import com.example.backend.repository.MemberRepository;
import com.example.backend.repository.ParticipationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ParticipationServiceTest {
    @InjectMocks
    private ParticipationService participationService;

    @Mock
    private ParticipationRepository participationRepository;
    @Mock
    private MeetingPostRepository meetingPostRepository;
    @Mock
    private MemberRepository memberRepository;

    private Member member;
    private MeetingPost meetingPost;
    private ParticipationRequestDto requestDto;

    @BeforeEach
    void setUp() {
        // 1. 주최자(Creator) 생성
        Member organizer = Member.builder().build();
        ReflectionTestUtils.setField(organizer, "id", 999L);

        // 2. 신청자(Member) 생성
        member = Member.builder().build();
        ReflectionTestUtils.setField(member, "id", 1L);

        // 3. 모임 게시글 생성 (제공해주신 생성자 구조 반영)
        meetingPost = MeetingPost.builder()
                .title("백엔드 스터디")
                .description("자바 CS 정복 모임")
                .capacity(5) // 정원
                .creator(organizer) // 주최자
                .build();
        ReflectionTestUtils.setField(meetingPost, "id", 100L);

        requestDto = new ParticipationRequestDto(100L, "열심히 참여하겠습니다!");
    }

    @Test
    @DisplayName("참여 신청 성공")
    void applySuccess() {
        // given
        given(meetingPostRepository.findById(any())).willReturn(Optional.of(meetingPost));
        given(memberRepository.findById(any())).willReturn(Optional.of(member));
        given(participationRepository.existsByMemberIdAndMeetingPostId(any(), any())).willReturn(false);
        given(participationRepository.countByMeetingPostAndStatus(any(), any())).willReturn(3L);

        // [추가] save 호출 시 저장될 객체를 그대로 반환하도록 설정
        // 만약 ID가 꼭 필요하다면 아래처럼 Mock 객체를 만들어 반환하게 합니다.
        Participation savedParticipation = Participation.builder().build();
        ReflectionTestUtils.setField(savedParticipation, "id", 500L); // 가짜 ID 심기

        given(participationRepository.save(any(Participation.class))).willReturn(savedParticipation);

        // when
        Long participationId = participationService.applyForMeeting(requestDto, 1L);

        // then
        assertThat(participationId).isEqualTo(500L); // 반환된 ID 검증
        verify(participationRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("중복 신청 시 예외 발생")
    void duplicateApplyFail() {
        // given
        given(meetingPostRepository.findById(any())).willReturn(Optional.of(meetingPost));
        given(memberRepository.findById(any())).willReturn(Optional.of(member));
        given(participationRepository.existsByMemberIdAndMeetingPostId(any(), any())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> participationService.applyForMeeting(requestDto, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 신청 중이거나 참여가 완료된 모임입니다."); // 실제 메시지로 수정
    }
}