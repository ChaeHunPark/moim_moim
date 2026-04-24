package com.example.backend.repository;

import com.example.backend.entity.Category;
import com.example.backend.entity.MeetingPost;
import com.example.backend.entity.Member;
import com.example.backend.entity.Participation;
import com.example.backend.enums.MemberStatus;
import com.example.backend.enums.ParticipationRole;
import com.example.backend.enums.ParticipationStatus;
import com.example.backend.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY) // H2 사용 강제
class MeetingPostRepositoryTest {



    @Autowired
    private MeetingPostRepository meetingPostRepository;

    @Autowired
    private TestEntityManager em;

    private Member host;
    private Member applicant;
    private Category studyCategory;

    @BeforeEach
    void setUp() {
    // 1. 방장(Host) 생성
        host = Member.builder()
                .nickname("방장")
                .email("host@test.com")
                .password("encoded_password") // 필수
                .role(Role.ROLE_USER)               // 필수 (엔티티의 Role Enum 사용)
                .status(MemberStatus.ACTIVE)   // ENUM 정의 준수
                .age(25)
                .points(0)
                .level(1)
                .build();

        // 2. 신청자(Applicant) 생성
        applicant = Member.builder()
                .nickname("신청자")
                .email("app@test.com")
                .password("encoded_password") // 필수
                .role(Role.ROLE_USER)               // 필수
                .status(MemberStatus.ACTIVE)   // 필수
                .age(27)
                .points(0)
                .level(1)
                .build();

        studyCategory = Category.builder().name("스터디").build();

        em.persist(host);
        em.persist(applicant);
        em.persist(studyCategory);

        // 2. 테스트용 게시글 생성
        MeetingPost post = MeetingPost.builder()
                .title("스터디 모집")
                .description("열공해요")
                .capacity(5)
                .startDate(LocalDateTime.now().plusDays(5))
                .creator(host)
                .category(studyCategory)
                .build();

        // 초기 인원 1명(방장) 반영되었다고 가정
        meetingPostRepository.save(post);

        // 3. 신청 정보(Participation) 생성 (참여 내역 조회 테스트용)

        // ⚠️ 방장이 본인 글에 참여한 게 아니라, 'applicant'가 'post'에 참여한 데이터
        Participation participation = Participation.builder()
                .member(applicant)
                .meetingPost(post)
                .status(ParticipationStatus.APPLIED)
                .role(ParticipationRole.PARTICIPANT)
                .build();
        em.persist(participation);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("상세 조회 테스트: fetch join으로 카테고리와 작성자를 한 번에 가져온다")
    void findByIdWithDetails() {
        // given - setUp에서 생성된 ID 찾기
        Long postId = meetingPostRepository.findAll().get(0).getId();

        // when
        MeetingPost foundPost = meetingPostRepository.findByIdWithDetails(postId)
                .orElseThrow();

        // then
        assertThat(foundPost.getTitle()).isEqualTo("스터디 모집");
        assertThat(foundPost.getCategory().getName()).isEqualTo("스터디"); // fetch join 확인
        assertThat(foundPost.getCreator().getNickname()).isEqualTo("방장"); // fetch join 확인
    }

    @Test
    @DisplayName("잔여석 순 정렬 테스트: 필터링 조건이 없을 때 전체 조회 확인")
    void findAllOrderByUrgent() {
        // when
        List<MeetingPost> result = meetingPostRepository.findAllOrderByUrgent(null);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getCategory()).isNotNull();
    }

    @Test
    @DisplayName("내가 신청한 모임 목록 조회: 방장이 아닌 경우만 조회되어야 한다")
    void findAllAppliedByMemberId() {
        // when: 'applicant'가 신청한 내역 조회
        List<Participation> results = meetingPostRepository.findAllAppliedByMemberId(applicant.getId());

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getMeetingPost().getTitle()).isEqualTo("스터디 모집");
        assertThat(results.get(0).getMeetingPost().getCreator().getId()).isNotEqualTo(applicant.getId());
    }

    @Test
    @DisplayName("방장이 본인 글을 조회할 때는 신청 목록에 나오지 않아야 한다")
    void findAllAppliedByMemberId_EmptyForHost() {
        // when: 'host'(방장)가 신청한 내역 조회
        List<Participation> results = meetingPostRepository.findAllAppliedByMemberId(host.getId());

        // then
        // 쿼리에 'mp.creator.id != :memberId' 조건이 있으므로 본인이 만든 글은 나오지 않음
        assertThat(results).isEmpty();
    }
}