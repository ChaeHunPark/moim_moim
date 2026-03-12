package com.example.backend.enums;

public enum MeetingSortCondition {
    LATEST,      // 최신순
    CLOSING,     // 마감 임박순 (startDate 기준)
    POPULAR,     // 인기순 (viewCount 기준)
    URGENT       // 잔여석 적은 순
}
