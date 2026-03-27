package com.example.backend.service;

import com.example.backend.common.exception.CustomException;
import com.example.backend.common.exception.ErrorCode;
import com.example.backend.dto.*;
import com.example.backend.entity.Category;
import com.example.backend.entity.MeetingPost;
import com.example.backend.entity.Member;
import com.example.backend.entity.Participation;
import com.example.backend.enums.ParticipationRole;
import com.example.backend.enums.ParticipationStatus;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.repository.MeetingPostRepository;
import com.example.backend.repository.MemberRepository;
import com.example.backend.repository.ParticipationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MeetingService {

    private final MeetingPostRepository meetingPostRepository;
    private final ParticipationRepository participationRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long createMeeting(MeetingPostCreateRequest request, Long memberId) {
        Member creator = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        // 2. 모임 생성 및 저장
        MeetingPost post = request.toEntity(creator, category);
        MeetingPost savedPost = meetingPostRepository.save(post);

        // 3. 방장을 참여자로 자동 등록 (MVP 핵심 로직)
        Participation organizer = Participation.builder()
                .member(creator)
                .meetingPost(savedPost)
                .role(ParticipationRole.ORGANIZER)
                .status(ParticipationStatus.ACCEPTED)
                .joinReason("모임 개설자 자동 등록")
                .build();

        participationRepository.save(organizer);

        return savedPost.getId();
    }

    /**
     * 모임 수정
     * @param meetingId 수정할 모임 ID
     * @param request   수정할 내용 (DTO)
     * @param memberId  현재 로그인한 유저 ID (작성자 검증용)
     */
    @Transactional
    public void updateMeeting(Long meetingId, MeetingPostUpdateRequest request, Long memberId) {
        MeetingPost post = meetingPostRepository.findById(meetingId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEETING_NOT_FOUND));

        // 권한 검증
        validateCreator(post, memberId);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        // 엔티티의 update 메서드로 데이터 위임
        post.update(
                request.getTitle(),
                request.getDescription(),
                request.getCapacity(),
                category,
                request.getStartDate(),
                request.getEndDate()
        );
    }
    /**
     * 모임 삭제
     */
    @Transactional
    public void deleteMeeting(Long meetingId, Long memberId) {
        // 1. 게시글 존재 여부 확인
        MeetingPost post = meetingPostRepository.findById(meetingId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEETING_NOT_FOUND));

        // 2. 작성자 권한 검증
        validateCreator(post, memberId);

        // 3. 삭제 수행 (연관된 참여 정보는 JPA Cascade 설정에 따라 처리)
        meetingPostRepository.delete(post);

        log.info("모임 삭제 완료 - ID: {}, 삭제자: {}", meetingId, memberId);
    }

    /**
     * 작성자 일치 여부 검증 공통 로직
     */
    private void validateCreator(MeetingPost post, Long memberId) {
        if (!post.getCreator().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.NOT_MEETING_CREATOR);
        }
    }


    @Transactional(readOnly = true)
    public MeetingDetailResponse getMeetingDetail(Long id, Long currentMemberId) {
        // 1. Fetch Join을 사용하여 Member와 Category를 한 번에 가져오는 레포지토리 메서드 호출
        MeetingPost post = meetingPostRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new CustomException(ErrorCode.MEETING_NOT_FOUND));

        post.incrementViewCount();


        boolean isHost = false;
        if (currentMemberId != null && post.getCreator() != null) {
            isHost = post.getCreator().getId().equals(currentMemberId);
        }

        return MeetingDetailResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .description(post.getDescription())       // content -> description
                .capacity(post.getCapacity())             // maxParticipants -> capacity
                .currentParticipants(post.getCurrentParticipants()) // 현재 참여 인원 추가
                .viewCount(post.getViewCount())           // 조회수 추가
                .categoryName(post.getCategory().getName())
                .categoryId(post.getCategory().getId())
                .creatorEmail(post.getCreator().getEmail())
                .startDate(post.getStartDate())           // deadline 대신 명확한 시작일
                .endDate(post.getEndDate())               // 종료일(마감일)
                .createdAt(post.getCreatedAt())
                .isHost(isHost)
                .build();
    }

    @Transactional(readOnly = true)
    public List<MeetingListResponse> getAllMeetings(String sortBy, Long categoryId) {
        List<MeetingPost> posts;

        // 1. 'urgent' (잔여석 순)은 별도 리포지토리 메서드 호출
        if ("urgent".equalsIgnoreCase(sortBy)) {
            posts = meetingPostRepository.findAllOrderByUrgent(categoryId);
        }
        // 2. 그 외 일반 정렬 (latest, closing, popular)
        else {
            Sort sort = switch (sortBy.toLowerCase()) {
                case "closing" -> Sort.by(Sort.Direction.ASC, "startDate");
                case "popular" -> Sort.by(Sort.Direction.DESC, "viewCount");
                default -> Sort.by(Sort.Direction.DESC, "createdAt");
            };

            if (categoryId != null) {
                posts = meetingPostRepository.findByCategoryId(categoryId, sort);
            } else {
                posts = meetingPostRepository.findAll(sort);
            }
        }

        return posts.stream()
                .map(MeetingListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 1. 내가 만든 모임 목록 조회
     **/

    @Transactional(readOnly = true)
    public List<MeetingSummaryResponse> getMyCreatedMeetings(Long memberId) {
        return meetingPostRepository.findByCreatorIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(post -> MeetingSummaryResponse.from(post, true)) // 팩토리 메서드 사용
                .collect(Collectors.toList());
    }

    /**
     * 2. 내가 신청한 모임 목록 조회 (참여자)
     */

    @Transactional(readOnly = true)
    public List<MeetingSummaryResponse> getMyAppliedMeetings(Long memberId) {
        return meetingPostRepository.findAllAppliedByMemberId(memberId).stream()
                .map(MeetingSummaryResponse::from) // 정적 팩토리 메서드 활용
                .collect(Collectors.toList());
    }





}
