package com.example.backend.service;


import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.enums.ParticipationRole;
import com.example.backend.enums.ParticipationStatus;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.repository.MeetingPostRepository;
import com.example.backend.repository.MemberRepository;
import com.example.backend.repository.ParticipationRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {
    @InjectMocks private MeetingService meetingService;

    @Mock private MeetingPostRepository meetingPostRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ParticipationRepository participationRepository;

    private Member testMember;
    private Category studyCategory;

    @BeforeEach
    void setUp() {
        // 1. 테스트용 회원 생성 (ID: 1L)
        testMember = Member.builder()
                .id(1L)
                .email("test@test.com")
                .nickname("테스터")
                .build();

        // 2. 카테고리 생성 (ID: 1L)
        // 엔티티에 ID setter가 없으므로 ReflectionTestUtils 사용
        studyCategory = Category.builder()
                .name("스터디")
                .build();
        ReflectionTestUtils.setField(studyCategory, "id", 1L);
    }

    // --- 모임 CRUD 테스트 (기존 create, update, delete 로직 유지) ---

    @Test
    @DisplayName("내가 만든 모임 목록 조회 성공")
    void getMyCreatedMeetings_success() {
        Long memberId = 1L;
        MeetingPost post = createPost("내가 만든 모임");
        given(meetingPostRepository.findByCreatorIdOrderByCreatedAtDesc(memberId)).willReturn(List.of(post));

        List<MeetingSummaryResponse> result = meetingService.getMyCreatedMeetings(memberId);

        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getTitle()).isEqualTo("내가 만든 모임");
    }

    @Test
    @DisplayName("내가 신청한 모임 목록 조회 성공")
    void getMyAppliedMeetings_success() {
        // 1. Given
        Long memberId = 1L;
        MeetingPost post = createPost("신청한 모임"); // 기존 헬퍼 메서드 활용
        Member member = Member.builder().id(memberId).build();

        // 엔티티 구조에 맞게 Builder 구성
        Participation participation = Participation.builder()
                .meetingPost(post)
                .member(member)
                .role(ParticipationRole.PARTICIPANT) // 참여자 역할
                .status(ParticipationStatus.ACCEPTED) // 승인 상태
                .joinReason("함께 공부하고 싶어서 신청합니다!")
                .build();

        given(meetingPostRepository.findAllAppliedByMemberId(memberId))
                .willReturn(List.of(participation));

        // 2. When
        List<MeetingSummaryResponse> result = meetingService.getMyAppliedMeetings(memberId);

        // 3. Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("신청한 모임");
        assertThat(result.get(0).getStatus()).isEqualTo("ACCEPTED");
        assertThat(result.get(0).isHost()).isFalse();
    }

    // 헬퍼 메서드: 카테고리와 작성자가 세팅된 기본 모임 객체 생성
    private MeetingPost createPost(String title) {
        return MeetingPost.builder()
                .title(title)
                .description("테스트 설명")
                .capacity(5)
                .creator(testMember)
                .category(studyCategory) // 위에서 만든 studyCategory 주입
                .build();
    }

    @Test
    @DisplayName("모임 상세 조회 시 최초 조회라면 조회수가 증가하고 쿠키가 생성된다")
    void getMeetingDetail_first_view_increases_count() {
        // 1. Given
        Long postId = 100L;
        MeetingPost post = createPost("최초 조회 테스트");
        ReflectionTestUtils.setField(post, "id", postId);
        ReflectionTestUtils.setField(post, "viewCount", 0); // 초기 조회수 0

        given(meetingPostRepository.findByIdWithDetails(postId)).willReturn(Optional.of(post));

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // 2. When
        MeetingDetailResponse result = meetingService.getMeetingDetail(postId, 1L, request, response);

        // 3. Then
        assertThat(result.getViewCount()).isEqualTo(1); // 조회수 증가 확인
        Cookie cookie = response.getCookie("postView");
        assertThat(cookie).isNotNull();
        assertThat(cookie.getValue()).contains("[" + postId + "]"); // 쿠키에 ID 포함 확인
    }

    @Test
    @DisplayName("이미 조회한 이력이 있는 쿠키가 있다면 조회수가 증가하지 않는다")
    void getMeetingDetail_duplicate_view_no_increase() {
        // 1. Given
        Long postId = 100L;
        MeetingPost post = createPost("중복 조회 테스트");
        ReflectionTestUtils.setField(post, "id", postId);
        ReflectionTestUtils.setField(post, "viewCount", 10); // 기존 조회수 10

        given(meetingPostRepository.findByIdWithDetails(postId)).willReturn(Optional.of(post));

        MockHttpServletRequest request = new MockHttpServletRequest();
        // 이미 해당 게시글을 본 이력이 있는 쿠키를 요청에 담음
        Cookie existingCookie = new Cookie("postView", "[" + postId + "]");
        request.setCookies(existingCookie);

        MockHttpServletResponse response = new MockHttpServletResponse();

        // 2. When
        MeetingDetailResponse result = meetingService.getMeetingDetail(postId, 1L, request, response);

        // 3. Then
        assertThat(result.getViewCount()).isEqualTo(10); // 조회수 그대로 (증가 X)
        // 기존 쿠키가 유지되거나 업데이트됨 (로직에 따라 다름)
    }
}