package com.example.backend.service;

import com.example.backend.dto.MeetingDetailResponse;
import com.example.backend.dto.MeetingPostCreateRequest;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MeetingService {

    private final MeetingPostRepository meetingPostRepository;
    private final ParticipationRepository participationRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long createMeeting(MeetingPostCreateRequest request, Long memberId) {
        // 1. 회원 및 카테고리 조회 (존재하지 않으면 404 에러 처리)
        Member creator = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다."));

        // 2. 모임 생성 및 저장
        MeetingPost post = request.toEntity(creator, category);
        MeetingPost savedPost = meetingPostRepository.save(post);

        // 3. 방장을 참여자로 자동 등록 (MVP 핵심 로직)
        Participation organizer = Participation.builder()
                .member(creator)
                .meetingPost(savedPost)
                .role(ParticipationRole.ORGANIZER)
                .status(ParticipationStatus.APPROVED)
                .build();

        participationRepository.save(organizer);

        return savedPost.getId();
    }

    @Transactional(readOnly = true)
    public MeetingDetailResponse getMeetingDetail(Long id) {
        // 1. Fetch Join을 사용하여 Member와 Category를 한 번에 가져오는 레포지토리 메서드 호출
        MeetingPost post = meetingPostRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 모임을 찾을 수 없습니다. ID: " + id));

        // 2. [수정] 일관성 있게 바뀐 필드명 매핑
        return MeetingDetailResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .description(post.getDescription())       // content -> description
                .capacity(post.getCapacity())             // maxParticipants -> capacity
                .currentParticipants(post.getCurrentParticipants()) // 현재 참여 인원 추가
                .viewCount(post.getViewCount())           // 조회수 추가
                .categoryName(post.getCategory().getName())
                .creatorEmail(post.getCreator().getEmail())
                .startDate(post.getStartDate())           // deadline 대신 명확한 시작일
                .endDate(post.getEndDate())               // 종료일(마감일)
                .createdAt(post.getCreatedAt())
                .build();
    }
}
