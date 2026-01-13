package com.example.backend.repository;

import com.example.backend.entity.MeetingPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MeetingPostRepository extends JpaRepository<MeetingPost, Long> {
    @Query("select m from MeetingPost m join fetch m.creator join fetch m.category where m.id = :id")
    Optional<MeetingPost> findByIdWithDetails(@Param("id") Long id);
}
