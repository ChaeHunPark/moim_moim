package com.example.backend.service;

import com.example.backend.common.exception.CustomException;
import com.example.backend.common.exception.ErrorCode;
import com.example.backend.dto.MeetingDetailResponse;
import com.example.backend.dto.MeetingListResponse;
import com.example.backend.dto.MeetingPostCreateRequest;
import com.example.backend.dto.MeetingPostUpdateRequest;
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
        // ID를 포함하여 빌드 (작성자 검증 시 필요)
        testMember = Member.builder().id(1L).email("test@test.com").build();
        studyCategory = Category.builder().name("스터디").build();
        ReflectionTestUtils.setField(studyCategory, "id", 1L);
    }

    // --- 1. 모임 생성 테스트 ---
    @Test
    @DisplayName("모임 생성 성공: 회원을 조회하고 모임을 저장한 뒤 방장을 등록한다")
    void createMeeting_success() {
        Long memberId = 1L;
        MeetingPostCreateRequest request = MeetingPostCreateRequest.builder()
                .categoryId(1L).capacity(5).build();
        MeetingPost savedPost = MeetingPost.builder().id(100L).build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(testMember));
        given(categoryRepository.findById(1L)).willReturn(Optional.of(studyCategory));
        given(meetingPostRepository.save(any(MeetingPost.class))).willReturn(savedPost);

        Long resultId = meetingService.createMeeting(request, memberId);

        assertThat(resultId).isEqualTo(100L);
        verify(participationRepository).save(any(Participation.class));
    }

    // --- 2. 상세 조회 테스트 (CustomException 반영) ---
    @Test
    @DisplayName("상세 조회 실패: 존재하지 않는 ID 조회 시 MEETING_NOT_FOUND 에러가 발생한다")
    void getMeetingDetail_fail_notFound() {
        // given: 상세 조회 시 결과가 없음
        given(meetingPostRepository.findByIdWithDetails(anyLong())).willReturn(Optional.empty());

        // when & then: 조회 시 이메일 인자를 추가 (null 혹은 "test@test.com")
        assertThatThrownBy(() -> meetingService.getMeetingDetail(999L, null))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEETING_NOT_FOUND);
    }

    // --- 3. 수정 테스트 (권한 및 비즈니스 로직 검증) ---
    @Test
    @DisplayName("모임 수정 성공: 작성자가 올바른 데이터를 보낼 때 정보가 업데이트된다")
    void updateMeeting_success() {
        Long meetingId = 100L;
        MeetingPost post = createPost("기존 제목");
        MeetingPostUpdateRequest request = MeetingPostUpdateRequest.builder()
                .title("수정된 제목").capacity(10).categoryId(1L).build();

        given(meetingPostRepository.findById(meetingId)).willReturn(Optional.of(post));
        given(categoryRepository.findById(1L)).willReturn(Optional.of(studyCategory));

        meetingService.updateMeeting(meetingId, request, 1L);

        assertThat(post.getTitle()).isEqualTo("수정된 제목");
        assertThat(post.getCapacity()).isEqualTo(10);
    }

    @Test
    @DisplayName("모임 수정 실패: 작성자가 아닌 유저가 접근하면 NOT_MEETING_CREATOR 에러가 발생한다")
    void updateMeeting_fail_notCreator() {
        Long meetingId = 100L;
        MeetingPost post = createPost("제목");
        given(meetingPostRepository.findById(meetingId)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> meetingService.updateMeeting(meetingId, any(), 999L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_MEETING_CREATOR);
    }

    @Test
    @DisplayName("모임 수정 실패: 정원을 현재 참여자보다 적게 수정하면 INVALID_CAPACITY 에러가 발생한다")
    void updateMeeting_fail_invalidCapacity() {
        Long meetingId = 100L;
        MeetingPost post = createPost("제목");
        ReflectionTestUtils.setField(post, "currentParticipants", 5); // 현재 5명

        MeetingPostUpdateRequest request = MeetingPostUpdateRequest.builder()
                .capacity(3).categoryId(1L).build(); // 3명으로 수정 시도

        given(meetingPostRepository.findById(meetingId)).willReturn(Optional.of(post));
        given(categoryRepository.findById(1L)).willReturn(Optional.of(studyCategory));

        assertThatThrownBy(() -> meetingService.updateMeeting(meetingId, request, 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CAPACITY);
    }

    // --- 4. 삭제 테스트 ---
    @Test
    @DisplayName("모임 삭제 성공: 작성자가 본인의 모임을 삭제할 수 있다")
    void deleteMeeting_success() {
        Long meetingId = 100L;
        MeetingPost post = createPost("삭제용");
        given(meetingPostRepository.findById(meetingId)).willReturn(Optional.of(post));

        meetingService.deleteMeeting(meetingId, 1L);

        verify(meetingPostRepository).delete(post);
    }

    @Test
    @DisplayName("모임 삭제 실패: 존재하지 않는 ID 삭제 시 MEETING_NOT_FOUND 에러가 발생한다")
    void deleteMeeting_fail_notFound() {
        given(meetingPostRepository.findById(anyLong())).willReturn(Optional.empty());

        assertThatThrownBy(() -> meetingService.deleteMeeting(999L, 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEETING_NOT_FOUND);
    }

    private MeetingPost createPost(String title) {
        return MeetingPost.builder()
                .title(title).creator(testMember).category(studyCategory).build();
    }
}