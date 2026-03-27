package com.example.backend.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import com.example.backend.common.exception.CustomException;
import com.example.backend.common.exception.ErrorCode;
import com.example.backend.dto.MemberResponse;
import com.example.backend.entity.Member;
import com.example.backend.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Nested
    @DisplayName("내 정보 조회 (getMyInfo)")
    class GetMyInfo {

        @Test
        @DisplayName("성공: 존재하는 ID로 조회 시 MemberResponse를 반환한다")
        void getMyInfo_success() {
            // given
            Long memberId = 1L;
            Member member = Member.builder()
                    .id(memberId)
                    .email("chaehoon@email.com")
                    .nickname("박채훈")
                    .build();

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

            // when
            MemberResponse result = memberService.getMyInfo(memberId);

            // then
            assertThat(result.getId()).isEqualTo(memberId);
            assertThat(result.getNickname()).isEqualTo("박채훈");
            assertThat(result.getEmail()).isEqualTo("chaehoon@email.com");
            verify(memberRepository).findById(memberId);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 ID로 조회 시 MEMBER_NOT_FOUND 예외가 발생한다")
        void getMyInfo_fail_notFound() {
            // given
            Long memberId = 999L;
            given(memberRepository.findById(memberId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.getMyInfo(memberId))
                    .isInstanceOf(CustomException.class) // 작성하신 커스텀 예외 클래스명으로 확인
                    .hasMessageContaining(ErrorCode.MEMBER_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("엔티티 조회 (getById)")
    class GetById {

        @Test
        @DisplayName("성공: 엔티티를 직접 반환한다")
        void getById_success() {
            // given
            Long memberId = 1L;
            Member member = Member.builder().id(memberId).build();
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

            // when
            Member result = memberService.getById(memberId);

            // then
            assertThat(result).isEqualTo(member);
        }
    }
}