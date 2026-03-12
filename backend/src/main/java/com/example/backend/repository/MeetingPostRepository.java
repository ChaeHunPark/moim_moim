package com.example.backend.repository;

import com.example.backend.entity.MeetingPost;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingPostRepository extends JpaRepository<MeetingPost, Long> {
    @Query("select m from MeetingPost m join fetch m.creator join fetch m.category where m.id = :id")
    Optional<MeetingPost> findByIdWithDetails(@Param("id") Long id);


    // 최신 생성일 순으로 전체 조회
    // N+1 방지를 위한 fetch join
    @Query("select m from MeetingPost m join fetch m.category join fetch m.creator order by m.createdAt desc")
    List<MeetingPost> findAllWithDetails();

    // 카테고리 필터링 + 기본 정렬
    List<MeetingPost> findByCategoryId(Long categoryId, Sort sort);

    // [특수 정렬] 잔여석 적은 순 (capacity - currentParticipants)
    @Query("SELECT m FROM MeetingPost m " +
            "WHERE m.category.id = :categoryId OR :categoryId IS NULL " +
            "ORDER BY (m.capacity - m.currentParticipants) ASC")
    List<MeetingPost> findAllOrderByUrgent(@Param("categoryId") Long categoryId);
}
