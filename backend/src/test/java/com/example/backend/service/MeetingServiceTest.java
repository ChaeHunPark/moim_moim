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
import org.junit.jupiter.api.Nested;
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

    private Member testMember;
    private Category studyCategory;

    @BeforeEach
    void setUp() {
        testMember = Member.builder().id(1L).email("test@test.com").nickname("테스터").build();

        studyCategory = Category.builder().name("스터디").build();
        ReflectionTestUtils.setField(studyCategory, "id", 1L);
    }

    // --- 리팩토링된 헬퍼 메서드 ---
    private MeetingPost createPost(Long id, String title, int viewCount) {
        MeetingPost post = MeetingPost.builder()
                .title(title)
                .description("테스트 설명")
                .capacity(5)
                .creator(testMember)
                .category(studyCategory)
                .build();
        ReflectionTestUtils.setField(post, "id", id);
        ReflectionTestUtils.setField(post, "viewCount", viewCount);
        return post;
    }

    private MeetingPost createPost(String title) {
        return createPost(1L, title, 0);
    }

    // --- 테스트 케이스 ---

    @Test
    @DisplayName("내가 만든 모임 목록 조회 성공")
    void getMyCreatedMeetings_success() {
        Long memberId = 1L;
        given(meetingPostRepository.findByCreatorIdOrderByCreatedAtDesc(memberId))
                .willReturn(List.of(createPost("내가 만든 모임")));

        List<MeetingSummaryResponse> result = meetingService.getMyCreatedMeetings(memberId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("내가 만든 모임");
    }

    @Test
    @DisplayName("내가 신청한 모임 목록 조회 성공")
    void getMyAppliedMeetings_success() {
        Long memberId = 1L;
        Participation participation = Participation.builder()
                .meetingPost(createPost("신청한 모임"))
                .member(testMember)
                .role(ParticipationRole.PARTICIPANT)
                .status(ParticipationStatus.ACCEPTED)
                .build();

        given(meetingPostRepository.findAllAppliedByMemberId(memberId)).willReturn(List.of(participation));

        List<MeetingSummaryResponse> result = meetingService.getMyAppliedMeetings(memberId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("ACCEPTED");
    }

    @Nested
    @DisplayName("상세 조회 및 조회수 테스트")
    class ViewCountTests {
        private final Long postId = 100L;
        private MockHttpServletRequest request;
        private MockHttpServletResponse response;

        @BeforeEach
        void init() {
            request = new MockHttpServletRequest();
            response = new MockHttpServletResponse();
        }

        @Test
        @DisplayName("최초 조회 시 조회수가 증가하고 쿠키가 생성된다")
        void increases_count_on_first_view() {
            MeetingPost post = createPost(postId, "최초 조회", 0);
            given(meetingPostRepository.findByIdWithDetails(postId)).willReturn(Optional.of(post));

            MeetingDetailResponse result = meetingService.getMeetingDetail(postId, 1L, request, response);

            assertThat(result.getViewCount()).isEqualTo(1);
            assertThat(response.getCookie("postView").getValue()).contains("[" + postId + "]");
        }

        @Test
        @DisplayName("이미 조회한 이력이 있으면 조회수가 유지된다")
        void no_increase_on_duplicate_view() {
            MeetingPost post = createPost(postId, "중복 조회", 10);
            given(meetingPostRepository.findByIdWithDetails(postId)).willReturn(Optional.of(post));
            request.setCookies(new Cookie("postView", "[" + postId + "]"));

            MeetingDetailResponse result = meetingService.getMeetingDetail(postId, 1L, request, response);

            assertThat(result.getViewCount()).isEqualTo(10);
        }
    }
}