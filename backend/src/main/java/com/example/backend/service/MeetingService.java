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

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private final NotificationService notificationService;

    @Transactional
    public Long createMeeting(MeetingPostCreateRequest request, Long memberId) {
        Member creator = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));


        // 엔티티에게 방장을 포함한 모임 생성 위힘
        MeetingPost post = MeetingPost.createMeeting(request, creator, category);

        return meetingPostRepository.save(post).getId();
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
        post.validateCreator(post, memberId);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        // 엔티티의 update 메서드로 데이터 위임
        post.update(request, category);
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
        post.validateCreator(post, memberId);

        // 3. 삭제 수행 (연관된 참여 정보는 JPA Cascade 설정에 따라 처리)
        meetingPostRepository.delete(post);

        log.info("모임 삭제 완료 - ID: {}, 삭제자: {}", meetingId, memberId);
    }


    @Transactional
    public MeetingDetailResponse getMeetingDetail(Long id, Long currentMemberId, HttpServletRequest request, HttpServletResponse response) {
        // 1. Fetch Join을 사용하여 Member와 Category를 한 번에 가져오는 레포지토리 메서드 호출
        MeetingPost post = meetingPostRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new CustomException(ErrorCode.MEETING_NOT_FOUND));

        // 2. 조회수 중복 방지 로직 (쿠키 활용)
        handleViewCountWithCookie(post, id, request, response);

        boolean isHost = post.isHost(currentMemberId);

        return MeetingDetailResponse.from(post, isHost);
    }

    /**
     * 쿠키를 사용하여 24시간 내 중복 조회를 방지하는 프라이빗 메서드
     *
     * 웹 기술에 의존적인 로직이라서 ViewCountManager같은 컴포넌트에 넣거나
     * 인터셉터/AOP로 분리 할 예정
     */
    private void handleViewCountWithCookie(MeetingPost post, Long postId, HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        Cookie viewCookie = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("postView")) {
                    viewCookie = cookie;
                    break;
                }
            }
        }

        if (viewCookie != null) {
            // 이미 해당 게시글을 본 적이 있는지 확인 (예: [1][2][15])
            if (!viewCookie.getValue().contains("[" + postId + "]")) {
                post.incrementViewCount();
                viewCookie.setValue(viewCookie.getValue() + "[" + postId + "]");
                viewCookie.setPath("/");
                viewCookie.setMaxAge(60 * 60 * 24); // 24시간 유지
                response.addCookie(viewCookie);
            }
        } else {
            // 쿠키가 아예 없는 경우 새로 생성
            post.incrementViewCount();
            Cookie newCookie = new Cookie("postView", "[" + postId + "]");
            newCookie.setPath("/");
            newCookie.setMaxAge(60 * 60 * 24);
            response.addCookie(newCookie);
        }
    }

    @Transactional(readOnly = true)
    public List<MeetingListResponse> getAllMeetings(String sortBy, Long categoryId) {
        // sort를 변수로 추출
        Sort sort = getSortOrder(sortBy);

        // 카테고리가 있다면 카테고리 + sort
        // 카테고리가 없다면 sort만
        List<MeetingPost> posts = (categoryId != null)
                ? meetingPostRepository.findByCategoryId(categoryId, sort)
                : meetingPostRepository.findAll(sort);

        // 'urgent' 케이스만 따로 처리하는 구조는 유지하되, 전체적으로 stream 활용
        if ("urgent".equalsIgnoreCase(sortBy)) {
            posts = meetingPostRepository.findAllOrderByUrgent(categoryId);
        }

        return posts.stream()
                .map(MeetingListResponse::from)
                .collect(Collectors.toList());
    }

    // 정렬 조건 생성 로직 분리 (가독성 향상)
    private Sort getSortOrder(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "closing" -> Sort.by(Sort.Direction.ASC, "startDate");
            case "popular" -> Sort.by(Sort.Direction.DESC, "viewCount");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
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
