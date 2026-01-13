package com.example.backend.entity;

import com.example.backend.enums.ParticipationRole;
import com.example.backend.enums.ParticipationStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Participation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_post_id", nullable = false)
    private MeetingPost meetingPost;

    @Enumerated(EnumType.STRING) // DB에 문자열로 저장 (가독성 및 안전성)
    @Column(nullable = false)
    private ParticipationRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipationStatus status;

    @Builder
    public Participation(Member member, MeetingPost meetingPost,
                         ParticipationRole role, ParticipationStatus status) {
        this.member = member;
        this.meetingPost = meetingPost;
        this.role = role;
        this.status = status;
    }
}