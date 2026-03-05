package com.example.backend.enums;

public enum ParticipationStatus {
    APPLIED,    // 신청 완료 (주최자 확인 전)
    ACCEPTED,   // 참여 승인 (정원에 포함됨)
    REJECTED,   // 참여 거절
    CANCELLED,  // 신청자 본인이 취소
    WAITING     // 정원 초과 시 대기 상태 (선택 사항)
}
