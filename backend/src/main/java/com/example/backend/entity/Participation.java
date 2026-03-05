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
    @JoinColumn(name = "meeting_post_id", nullable = false)
    private MeetingPost meetingPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipationRole role; // PARTICIPANT, ORGANIZER

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipationStatus status; // APPLIED, CANCELLED, ACCEPTED, REJECTED, WAITING

    @Column(columnDefinition = "TEXT")
    private String joinReason;

    @Column(nullable = false)
    private boolean ratingGiven = false; // 기본값 false

    @Builder
    public Participation(MeetingPost meetingPost, Member member, ParticipationRole role,
                         ParticipationStatus status, String joinReason) {
        this.meetingPost = meetingPost;
        this.member = member;
        this.role = role;
        this.status = status;
        this.joinReason = joinReason;
        this.ratingGiven = false;
    }
}