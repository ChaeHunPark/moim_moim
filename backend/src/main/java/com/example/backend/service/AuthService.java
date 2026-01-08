package com.example.backend.service;

import com.example.backend.dto.RegisterRequest;
import com.example.backend.entity.Member;
import com.example.backend.entity.Region;
import com.example.backend.enums.MemberStatus;
import com.example.backend.enums.Role;
import com.example.backend.repository.MemberRepository;
import com.example.backend.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final RegionRepository regionRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegisterRequest request) {
        // 1. 중복 체크
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 2. 지역 조회
        Region region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역입니다."));

        // 3. 명세서에 따른 회원 객체 생성 (기본값 설정)
        Member member = Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // 아직 암호화 전 (평문 테스트용)
                .nickname(request.getNickname())
                .age(request.getAge())
                .introduction(request.getBio()) // 프론트의 bio 필드 매핑
                .region(region)
                .role(Role.USER)            // 기본 권한: USER
                .status(MemberStatus.ACTIVE) // 기본 상태: ACTIVE
                .points(0)                  // 초기 포인트: 0
                .level(1)                   // 초기 레벨: 1
                .build();

        // 4. DB 저장 (createdAt, updatedAt은 자동 입력됨)
        memberRepository.save(member);
    }
}
