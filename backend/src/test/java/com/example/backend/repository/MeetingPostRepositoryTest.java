package com.example.backend.repository;

import com.example.backend.entity.Category;
import com.example.backend.entity.MeetingPost;
import com.example.backend.entity.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class MeetingPostRepositoryTest {

    @Autowired
    private MeetingPostRepository meetingPostRepository;

    @Autowired
    private TestEntityManager em; // 연관 객체 저장을 위해 사용

    private Member testMember;
    private Category studyCategory;
    private Category exerciseCategory;

    @BeforeEach
    void setUp() {
        // 1. 공통 연관 객체 생성 및 저장
        testMember = Member.builder().email("test@test.com").nickname("훈").build();
        studyCategory = Category.builder().name("스터디").build();
        exerciseCategory = Category.builder().name("운동").build();

        em.persist(testMember);
        em.persist(studyCategory);
        em.persist(exerciseCategory);

        // 2. 다양한 조건의 테스트 데이터 생성

        // [Post A] 최신글, 시작일 멀음, 조회수 낮음, 잔여석 적음 (4/5)
        MeetingPost postA = MeetingPost.builder()
                .title("Post A (스터디)")
                .description("내용 A")
                .capacity(5)
                .startDate(LocalDateTime.now().plusDays(10)) // 10일 뒤 시작
                .creator(testMember)
                .category(studyCategory)
                .build();
        for(int i=0; i<3; i++) postA.addParticipant(); // 기본 1명 + 3명 = 4명 (잔여 1)

        // [Post B] 과거글, 시작일 빠름, 조회수 높음, 잔여석 많음 (2/10)
        MeetingPost postB = MeetingPost.builder()
                .title("Post B (운동)")
                .description("내용 B")
                .capacity(10)
                .startDate(LocalDateTime.now().plusDays(2)) // 2일 뒤 시작 (임박)
                .creator(testMember)
                .category(exerciseCategory)
                .build();
        postB.addParticipant(); // 기본 1명 + 1명 = 2명 (잔여 8)
        for(int i=0; i<50; i++) postB.incrementViewCount(); // 조회수 50

        meetingPostRepository.save(postA);
        meetingPostRepository.save(postB);

        em.flush(); // DB에 반영
        em.clear(); // 영속성 컨텍스트 비우기 (실제 DB 조회 확인을 위해)
    }

    @Test
    @DisplayName("마감 임박순 정렬 테스트: 시작일이 빠른 Post B가 먼저 나와야 한다")
    void sortByClosingSoon() {
        List<MeetingPost> result = meetingPostRepository.findAll(Sort.by(Sort.Direction.ASC, "startDate"));

        assertThat(result.get(0).getTitle()).isEqualTo("Post B (운동)");
    }

    @Test
    @DisplayName("인기순 정렬 테스트: 조회수가 높은 Post B가 먼저 나와야 한다")
    void sortByPopular() {
        List<MeetingPost> result = meetingPostRepository.findAll(Sort.by(Sort.Direction.DESC, "viewCount"));

        assertThat(result.get(0).getViewCount()).isEqualTo(50);
    }

    @Test
    @DisplayName("잔여석 적은 순 정렬 테스트: 1자리 남은 Post A가 먼저 나와야 한다")
    void sortByUrgent() {
        // 직접 작성한 @Query 호출
        List<MeetingPost> result = meetingPostRepository.findAllOrderByUrgent(null);

        int remainA = result.get(0).getCapacity() - result.get(0).getCurrentParticipants();
        assertThat(remainA).isEqualTo(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Post A (스터디)");
    }

    @Test
    @DisplayName("카테고리 필터링 테스트: 운동 카테고리 선택 시 Post B만 나와야 한다")
    void filterByCategory() {
        List<MeetingPost> result = meetingPostRepository.findByCategoryId(
                exerciseCategory.getId(), Sort.by(Sort.Direction.DESC, "createdAt")
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory().getName()).isEqualTo("운동");
    }
}