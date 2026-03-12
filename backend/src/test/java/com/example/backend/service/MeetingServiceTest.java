package com.example.backend.service;

import com.example.backend.dto.MeetingDetailResponse;
import com.example.backend.dto.MeetingListResponse;
import com.example.backend.dto.MeetingPostCreateRequest;
import com.example.backend.entity.*;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.repository.MeetingPostRepository;
import com.example.backend.repository.MemberRepository;
import com.example.backend.repository.ParticipationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {


    @Mock private MeetingPostRepository meetingPostRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private ParticipationRepository participationRepository;
    @Mock private CategoryRepository categoryRepository;

    @InjectMocks
    private MeetingService meetingService;

    private Member testMember;
    private Category studyCategory;

    @BeforeEach
    void setUp() {
        testMember = Member.builder().email("test@test.com").build();
        studyCategory = Category.builder().name("스터디").build();
        // ID가 필요한 경우 Reflection으로 주입 (카테고리 ID 필터링 테스트용)
        ReflectionTestUtils.setField(studyCategory, "id", 1L);
    }

    // --- 1. 모임 생성 테스트 ---

    @Test
    @DisplayName("모임 생성 성공: 회원을 조회하고 모임을 저장한 뒤 방장을 등록한다")
    void createMeeting_success() {
        // [Given]
        Long memberId = 1L;
        Long categoryId = 10L;
        Member creator = Member.builder().id(memberId).email("test@test.com").build();
        Category category = Category.builder().name("운동").build();

        MeetingPostCreateRequest request = MeetingPostCreateRequest.builder()
                .title("테스트 모임")
                .description("설명")
                .capacity(5)
                .categoryId(categoryId)
                .build();

        MeetingPost savedPost = MeetingPost.builder().id(100L).build();

        // given - willReturn 스타일 적용 (수정 완료)
        given(memberRepository.findById(memberId)).willReturn(Optional.of(creator));
        given(categoryRepository.findById(request.getCategoryId())).willReturn(Optional.of(category));

        // .class 리터럴을 직접 사용하세요!
        given(meetingPostRepository.save(any(MeetingPost.class))).willReturn(savedPost);

        // [When]
        Long resultId = meetingService.createMeeting(request, memberId);

        // [Then]
        assertThat(resultId).isEqualTo(100L);
        verify(meetingPostRepository, times(1)).save(any(MeetingPost.class));
        verify(participationRepository, times(1)).save(any(Participation.class));
    }

    // --- 2. 모임 상세 조회 테스트 ---

    @Test
    @DisplayName("상세 조회 성공: Fetch Join 결과를 Response DTO로 올바르게 변환한다")
    void getMeetingDetail_success() {
        // [Given]
        Long meetingId = 100L;
        Member creator = Member.builder().email("owner@test.com").build();
        Category category = Category.builder().name("코딩").build();

        MeetingPost post = MeetingPost.builder()
                .title("자바 스터디")
                .description("열심히 하실 분")
                .capacity(4)
                .creator(creator)
                .category(category)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(7))
                .build();

        // Repository의 커스텀 메서드 mocking
        given(meetingPostRepository.findByIdWithDetails(meetingId)).willReturn(Optional.of(post));

        // [When]
        MeetingDetailResponse response = meetingService.getMeetingDetail(meetingId);

        // [Then]
        assertThat(response.getTitle()).isEqualTo("자바 스터디");
        assertThat(response.getCreatorEmail()).isEqualTo("owner@test.com");
        assertThat(response.getCategoryName()).isEqualTo("코딩");
        assertThat(response.getCurrentParticipants()).isEqualTo(1); // 엔티티 기본값 1 확인
    }

    @Test
    @DisplayName("상세 조회 실패: 존재하지 않는 ID로 조회 시 EntityNotFoundException이 발생한다")
    void getMeetingDetail_fail_notFound() {
        // [Given]
        Long invalidId = 999L;
        given(meetingPostRepository.findByIdWithDetails(invalidId)).willReturn(Optional.empty());

        // [When & Then]
        assertThatThrownBy(() -> meetingService.getMeetingDetail(invalidId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("해당 모임을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("최신순(latest) 조회 시 생성일 내림차순 정렬이 적용되어야 한다")
    void getAllMeetings_Latest() {
        // given
        MeetingPost post = createPost("최신글");
        ReflectionTestUtils.setField(post, "id", 1L);
        given(meetingPostRepository.findAll(any(Sort.class))).willReturn(List.of(post));

        // when
        List<MeetingListResponse> result = meetingService.getAllMeetings("latest", null);

        // then
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(meetingPostRepository).findAll(sortCaptor.capture());

        assertThat(sortCaptor.getValue().getOrderFor("createdAt").getDirection())
                .isEqualTo(Sort.Direction.DESC);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("인기순(popular) 조회 시 조회수 내림차순 정렬이 적용되어야 한다")
    void getAllMeetings_Popular() {
        // given
        given(meetingPostRepository.findAll(any(Sort.class))).willReturn(List.of());

        // when
        meetingService.getAllMeetings("popular", null);

        // then
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(meetingPostRepository).findAll(sortCaptor.capture());

        assertThat(sortCaptor.getValue().getOrderFor("viewCount").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("마감 임박순(closing) 조회 시 시작일 오름차순 정렬이 적용되어야 한다")
    void getAllMeetings_Closing() {
        // given
        given(meetingPostRepository.findAll(any(Sort.class))).willReturn(List.of());

        // when
        meetingService.getAllMeetings("closing", null);

        // then
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(meetingPostRepository).findAll(sortCaptor.capture());

        assertThat(sortCaptor.getValue().getOrderFor("startDate").getDirection())
                .isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @DisplayName("잔여석 순(urgent) 조회 시 커스텀 리포지토리 메서드가 호출되어야 한다")
    void getAllMeetings_Urgent() {
        // given
        given(meetingPostRepository.findAllOrderByUrgent(any())).willReturn(List.of());

        // when
        meetingService.getAllMeetings("urgent", 1L);

        // then
        // urgent는 일반 findAll(Sort)이 아닌 특수 쿼리 메서드 호출 확인
        verify(meetingPostRepository).findAllOrderByUrgent(1L);
    }

    @Test
    @DisplayName("카테고리 ID가 있으면 카테고리 필터링 메서드가 호출되어야 한다")
    void getAllMeetings_WithCategory() {
        // given
        given(meetingPostRepository.findByCategoryId(anyLong(), any(Sort.class))).willReturn(List.of());

        // when
        meetingService.getAllMeetings("latest", 1L);

        // then
        verify(meetingPostRepository).findByCategoryId(eq(1L), any(Sort.class));
    }

    // 테스트용 헬퍼 메서드
    private MeetingPost createPost(String title) {
        return MeetingPost.builder()
                .title(title)
                .description("내용")
                .capacity(5)
                .creator(testMember)
                .category(studyCategory)
                .build();
    }
}